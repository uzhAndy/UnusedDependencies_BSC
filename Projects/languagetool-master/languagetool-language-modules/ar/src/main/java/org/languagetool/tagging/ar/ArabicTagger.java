/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.tagging.ar;

import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Arabic;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.ArabicStringTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @since 4.9
 */
public class ArabicTagger extends BaseTagger {

  private final ArabicTagManager tagmanager = new ArabicTagManager();
  private boolean newStylePronounTag = false;

  public ArabicTagger() {
    super("/ar/arabic.dict", new Locale("ar"));
  }


  @Override
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) {
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    IStemmer dictLookup = new DictionaryLookup(getDictionary());
    int pos = 0;
    for (String word : sentenceTokens) {
      List<AnalyzedToken> l = new ArrayList<>();
      String striped = ArabicStringTools.removeTashkeel(word);
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(striped));
      addTokens(taggerTokens, l);
      // additional tagging with prefixes
      // if not a stop word add more stemming
      if (!isStopWord(taggerTokens)) {
        // test all possible tags
        addTokens(additionalTags(word, dictLookup), l);
      }
      if (l.isEmpty()) {
        l.add(new AnalyzedToken(word, null, null));
      }
      tokenReadings.add(new AnalyzedTokenReadings(l, pos));
      pos += word.length();
    }
    return tokenReadings;
  }

  @Nullable
  protected List<AnalyzedToken> additionalTags(String word, IStemmer stemmer) {
    String striped = ArabicStringTools.removeTashkeel(word);
    List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
    List<Integer> prefix_index_list = getPrefixIndexList(striped);
    List<Integer> suffix_index_list = getSuffixIndexList(striped);

    for (int i : prefix_index_list) {
      for (int j : suffix_index_list) {
        // avoid default case of returned word as it
        if ((i == 0) && (j == striped.length()))
          continue;
        // get stem return a list, to generate some variants for stems.
        List<String> stemsList = getStem(striped, i, j);
        List<String> tags = getTags(striped, i, j);

        for (String stem : stemsList) {
          List<AnalyzedToken> taggerTokens;
          taggerTokens = asAnalyzedTokenList(stem, stemmer.lookup(stem));

          for (AnalyzedToken taggerToken : taggerTokens) {
            String posTag = taggerToken.getPOSTag();
            // modify tags in postag, return null if not compatible
            posTag = tagmanager.modifyPosTag(posTag, tags);

            if (posTag != null)
              additionalTaggedTokens.add(new AnalyzedToken(word, posTag, taggerToken.getLemma()));
          } // for taggerToken
        } // for stem in stems
      } // for j
    } // for i
    return additionalTaggedTokens;
  }

  private void addTokens(List<AnalyzedToken> taggedTokens, List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }


  private List<Integer> getSuffixIndexList(String possibleWord) {
    List<Integer> suffix_indexes = new ArrayList<>();
    suffix_indexes.add(possibleWord.length());
    int suffix_pos = possibleWord.length();
    if (possibleWord.endsWith("??")
      || possibleWord.endsWith("????")
      || possibleWord.endsWith("??????")
      || possibleWord.endsWith("??????")
      || possibleWord.endsWith("????")
      || possibleWord.endsWith("????")
      || possibleWord.endsWith("????")
      || possibleWord.endsWith("????")
      || possibleWord.endsWith("????")
    ) {
      if (possibleWord.endsWith("??"))
        suffix_pos -= 1;
      else if (possibleWord.endsWith("??????") || possibleWord.endsWith("??????"))
        suffix_pos -= 3;
      else
        suffix_pos -= 2;
      suffix_indexes.add(suffix_pos);
    }
    return suffix_indexes;
  }

  private List<Integer> getPrefixIndexList(String possibleWord) {
    List<Integer> prefix_indexes = new ArrayList<>();
    prefix_indexes.add(0);
    int prefix_pos;

    // four letters
    if (possibleWord.startsWith("????????")
      || possibleWord.startsWith("????????")
      || possibleWord.startsWith("????????")
      || possibleWord.startsWith("????????")
    ) {
      prefix_pos = 4;
      prefix_indexes.add(prefix_pos);
    }

    // three letters
    if (possibleWord.startsWith("??????") // ?????? ???????? ????+????
      || possibleWord.startsWith("??????")
      || possibleWord.startsWith("??????")
      || possibleWord.startsWith("??????")
      || possibleWord.startsWith("??????")
      || possibleWord.startsWith("??????")
    ) {
      prefix_pos = 3;
      prefix_indexes.add(prefix_pos);
    }

    // two letters
    if (possibleWord.startsWith("????")  // ???????? ??+???? ???? ?????????? ????????????
      || possibleWord.startsWith("????")
      || possibleWord.startsWith("????")
      || possibleWord.startsWith("????")
      || possibleWord.startsWith("????")
      || possibleWord.startsWith("????")
      || possibleWord.startsWith("????")
      || possibleWord.startsWith("????")
      //  ???????? ?????????? ???????????????? ?????????? ?????? ???? ????????
      || possibleWord.startsWith("??????")
      || possibleWord.startsWith("??????")
      || possibleWord.startsWith("??????")
      || possibleWord.startsWith("??????")
      || possibleWord.startsWith("??????")
      || possibleWord.startsWith("??????")
      || possibleWord.startsWith("??????")
      || possibleWord.startsWith("??????")

    ) {
      prefix_pos = 2;
      prefix_indexes.add(prefix_pos);
    }

    // one letter
    if (possibleWord.startsWith("??")
      || possibleWord.startsWith("??")
      || possibleWord.startsWith("??")
      || possibleWord.startsWith("??")
      || possibleWord.startsWith("??")
      || possibleWord.startsWith("????")  //  ???????? ?????????? ???????????????? ?????????? ?????? ???? ????????
      || possibleWord.startsWith("????")
      || possibleWord.startsWith("????")
      || possibleWord.startsWith("????")
    ) {
      prefix_pos = 1;
      prefix_indexes.add(prefix_pos);
    }

    // get prefixe
    return prefix_indexes;
  }


  private List<String> getTags(String word, int posStart, int posEnd) {
    List<String> tags = new ArrayList<>();
    // extract tags from word
    String prefix = getPrefix(word, posStart);
    String suffix = getSuffix(word, posEnd);
    // prefixes
    // first place
    if (prefix.startsWith("??") || prefix.startsWith("??")) {
      tags.add("CONJ;W");
      prefix = prefix.replaceAll("^[????]", "");

    }
    // second place
    if (prefix.startsWith("??")) {
      tags.add("JAR;K");
    } else if (prefix.startsWith("??")) {
      tags.add("JAR;L");
    } else if (prefix.startsWith("??")) {
      tags.add("JAR;B");
    } else if (prefix.startsWith("??")) {
      tags.add("ISTIQBAL;S");
    }
    // last place
    if (prefix.endsWith("????")
      || prefix.endsWith("????")
    ) {
      tags.add("PRONOUN;D");
    }
    // suffixes
    if (!newStylePronounTag) {
      switch (suffix) {
        case "????":
        case "????":
        case "??":
        case "??????":
        case "????":
        case "????":
        case "??":
        case "????":
        case "??????":
        case "????":
        case "????":
          tags.add("PRONOUN;H");
          break;
      }
    } else // if newStyle
    {
      switch (suffix) {
        case "????":
          tags.add("PRONOUN;b");
          break;
        case "????":
          tags.add("PRONOUN;c");
          break;
        case "??":
          tags.add("PRONOUN;d");
          break;
        case "??????":
          tags.add("PRONOUN;e");
          break;
        case "????":
          tags.add("PRONOUN;f");
          break;
        case "????":
          tags.add("PRONOUN;g");
          break;
        case "??":
          tags.add("PRONOUN;H");
          break;
        case "????":
          tags.add("PRONOUN;i");
          break;
        case "??????":
          tags.add("PRONOUN;j");
          break;
        case "????":
          tags.add("PRONOUN;k");
          break;
        case "????":
          tags.add("PRONOUN;n");
      }
    }


    return tags;
  }

  /**
   * @return test if word has stopword tagging
   */
  private boolean isStopWord(List<AnalyzedToken> taggerTokens) {
    // if one token is stop word
    for (AnalyzedToken tok : taggerTokens) {
      if (tok != null && tagmanager.isStopWord(tok.getPOSTag())) {
        return true;
      }
    }
    return false;
  }

  private String getPrefix(String word, int pos) {
    return word.substring(0, pos);
  }

  private String getSuffix(String word, int pos) {
    return word.substring(pos);
  }

  private List<String> getStem(String word, int posStart, int posEnd) {
    // get prefix
    // extract only stem+suffix, the suffix ill be replaced by pronoun model
    List<String> stemList = new ArrayList<>();
    String stem = word.substring(posStart);
    if (posEnd != word.length()) { // convert attached pronouns to one model form
      stem = stem.replaceAll("(??|????|??????|????|????|??????|????|????|????|??)$", "??");
    }
    // correct some stems
    // correct case of ??????????
    String prefix = getPrefix(word, posStart);
    if (prefix.endsWith("????")) {
      stemList.add("??" + stem); // ?????????? => ????- ????????
    }

    stemList.add(stem);  // ???????? ???? ??+????????
    return stemList;
  }


  /*
   * a temporary attribute to be compatible with existing pronoun tag style
   */
  public void enableNewStylePronounTag() {
    newStylePronounTag = true;
  }
}

