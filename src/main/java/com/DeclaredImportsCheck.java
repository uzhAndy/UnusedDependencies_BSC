package com;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.apache.maven.model.Dependency;

import java.util.ArrayList;

public class DeclaredImportsCheck extends AbstractCheck {

    private ArrayList<Class> importedClasses = new ArrayList<>();



    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.IMPORT};
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[0];
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[0];
    }

    @Override
    public void visitToken(DetailAST ast){

    }
}
