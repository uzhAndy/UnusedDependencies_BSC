/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;
import org.languagetool.tagging.Tagger;
import org.languagetool.tools.StringTools;

public abstract class AbstractFindSuggestionsFilter extends RuleFilter {

  final private int MAX_SUGGESTIONS = 10;

  abstract protected Tagger getTagger();

  abstract protected MorfologikSpeller getSpeller();

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {

    List<String> replacements = new ArrayList<>();
    String wordFrom = getRequired("wordFrom", arguments);
    String desiredPostag = getRequired("desiredPostag", arguments);
    String removeSuggestionsRegexp = getOptional("removeSuggestionsRegexp", arguments);
    // diacriticsMode: return only changes in diacritics. If there is none, the
    // match is removed.
    String mode = getOptional("Mode", arguments);
    boolean diacriticsMode = (mode != null) && mode.equals("diacritics");
    boolean generateSuggestions = true;
    Pattern regexpPattern = null;

    if (wordFrom != null && desiredPostag != null) {
      int posWord = 0;
      if (wordFrom.equals("marker")) {
        while (posWord < patternTokens.length && patternTokens[posWord].getStartPos() < match.getFromPos()) {
          posWord++;
        }
        posWord++;
      } else {
        posWord = Integer.parseInt(wordFrom);
      }
      if (posWord < 1 || posWord > patternTokens.length) {
        throw new IllegalArgumentException("FindSuggestionsFilter: Index out of bounds in "
            + match.getRule().getFullId() + ", PronounFrom: " + posWord);
      }
      AnalyzedTokenReadings atrWord = patternTokens[posWord - 1];
      boolean isWordCapitalized = StringTools.isCapitalizedWord(atrWord.getToken());
      boolean isWordAllupper = StringTools.isAllUppercase(atrWord.getToken());

      // Check if the original token (before disambiguation) meets the requirements
      List<String> originalWord = Collections.singletonList(atrWord.getToken());
      List<AnalyzedTokenReadings> aOriginalWord = getTagger().tag(originalWord);
      for (AnalyzedTokenReadings atr : aOriginalWord) {
        if (atr.matchesPosTagRegex(desiredPostag)) {
          if (diacriticsMode) {
            return null;
          } else {
            generateSuggestions = false;
          }
        }
      }

      if (generateSuggestions) {
        if (removeSuggestionsRegexp != null) {
          regexpPattern = Pattern.compile(removeSuggestionsRegexp, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }
        String wordToCheck = atrWord.getToken();
        if (atrWord.isTagged()) {
          wordToCheck = makeWrong(atrWord.getToken());
        }
        List<String> suggestions = getSpeller().findReplacements(wordToCheck);
        if (suggestions.size() > 0) {
          // TODO: do not tag capitalized words with tags for lower case
          List<AnalyzedTokenReadings> analyzedSuggestions = getTagger().tag(suggestions);
          for (AnalyzedTokenReadings analyzedSuggestion : analyzedSuggestions) {
            if (!analyzedSuggestion.getToken().equals(atrWord.getToken())
                && analyzedSuggestion.matchesPosTagRegex(desiredPostag)) {
              if (!replacements.contains(analyzedSuggestion.getToken())
                  && !replacements.contains(analyzedSuggestion.getToken().toLowerCase())
                  && (!diacriticsMode || equalWithoutDiacritics(analyzedSuggestion.getToken(), atrWord.getToken()))) {
                if (regexpPattern == null || !regexpPattern.matcher(analyzedSuggestion.getToken()).matches()) {
                  String replacement = analyzedSuggestion.getToken();
                  if (isWordAllupper) {
                    replacement = replacement.toUpperCase();
                  }
                  if (isWordCapitalized) {
                    replacement = StringTools.uppercaseFirstChar(replacement);
                  }
                  replacements.add(replacement);
                }
              }
              if (replacements.size() >= MAX_SUGGESTIONS) {
                break;
              }
            }
          }
        }
      }
    }

    if (diacriticsMode && replacements.size() == 0) {
      return null;
    }

    String message = match.getMessage();
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(),
        message, match.getShortMessage());
    ruleMatch.setType(match.getType());

    List<String> definitiveReplacements = new ArrayList<>();
    boolean replacementsUsed = false;
    if (generateSuggestions) {
      for (String s : match.getSuggestedReplacements()) {
        if (s.contains("{suggestion}")) {
          replacementsUsed = true;
          for (String s2 : replacements) {
            if (definitiveReplacements.size() >= MAX_SUGGESTIONS) {
              break;
            }
            definitiveReplacements.add(s.replace("{suggestion}", s2));
          }
        } else {
          definitiveReplacements.add(s);
        }
      }
      if (!replacementsUsed) {
        definitiveReplacements.addAll(replacements);
      }
    }

    if (!definitiveReplacements.isEmpty()) {
      ruleMatch.setSuggestedReplacements(definitiveReplacements);
    }
    return ruleMatch;
  }

  /*
   * Invent a wrong word to find possible replacements. This is a hack to obtain
   * suggestions from the speller when the original word is a correct word.
   */
  private String makeWrong(String s) {
    if (s.contains("a")) {
      return s.replace("a", "??");
    }
    if (s.contains("e")) {
      return s.replace("e", "??");
    }
    if (s.contains("i")) {
      return s.replace("i", "??");
    }
    if (s.contains("o")) {
      return s.replace("o", "??");
    }
    if (s.contains("u")) {
      return s.replace("u", "??");
    }
    if (s.contains("??")) {
      return s.replace("??", "??");
    }
    if (s.contains("??")) {
      return s.replace("??", "??");
    }
    if (s.contains("??")) {
      return s.replace("??", "??");
    }
    if (s.contains("??")) {
      return s.replace("??", "??");
    }
    if (s.contains("??")) {
      return s.replace("??", "??");
    }
    if (s.contains("??")) {
      return s.replace("??", "??");
    }
    if (s.contains("??")) {
      return s.replace("??", "??");
    }
    if (s.contains("??")) {
      return s.replace("??", "i");
    }
    if (s.contains("??")) {
      return s.replace("??", "??");
    }
    if (s.contains("??")) {
      return s.replace("??", "??");
    }
    if (s.contains("??")) {
      return s.replace("??", "??");
    }
    return s + "-";
  }

  private boolean equalWithoutDiacritics(String s, String t) {
    return StringTools.removeDiacritics(s).equalsIgnoreCase(StringTools.removeDiacritics(t));
  }
}
