package com.Components;

import com.github.javaparser.ast.ImportDeclaration;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import org.apache.maven.model.Dependency;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ProjectFile extends File {

    private ArrayList<FullIdent> declaredImportsFullIndent = new ArrayList<>();
    private ArrayList<ImportDeclaration> declaredImports = new ArrayList<>();

    private ArrayList<FullIdent> usedImportsFullIndents = new ArrayList<>();
    private ArrayList<ImportDeclaration> usedImports = new ArrayList<>();

    private ArrayList<FullIdent> unUsedImportsFullIndents = new ArrayList<>();
    private ArrayList<ImportDeclaration> unUsedImports = new ArrayList<>();

    private ArrayList<Dependency> usedDependencies = new ArrayList<>();

    private DetailAST projectAST;

    private String package_;

    public ProjectFile(String pathname){
        super(pathname);
        try{
            this.projectAST = JavaParser.parseFile(this.getAbsoluteFile(), JavaParser.Options.WITHOUT_COMMENTS);
            this.classifyImports();
            this.convertSetOfFullIndentToArrayListOfImportDeclaration();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static ProjectFile initializeProjectFile(File file){
        try{
            return new ProjectFile(file.getAbsolutePath());
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public void addUsedDependency(Dependency dependency){
        this.usedDependencies.add(dependency);
    }

    public DetailAST getProjectAST() {
        return projectAST;
    }

    private void classifyImports(){

        ArrayList<DetailAST> listAST = getIndividualAST(this.projectAST);

        UnusedImportsCheck unusedImportsCheck = new UnusedImportsCheck();
        unusedImportsCheck.setProcessJavadoc(false);

        unusedImportsCheck.beginTree(this.projectAST);

        for(DetailAST detailAST: listAST){
            unusedImportsCheck.visitToken(detailAST);
            unusedImportsCheck.leaveToken(detailAST);
        }

        unusedImportsCheck.finishTree(this.projectAST);

        this.usedImportsFullIndents = unusedImportsCheck.getUsedImports();
        this.unUsedImportsFullIndents = unusedImportsCheck.getUnUsedImports();
    }

    private void convertSetOfFullIndentToArrayListOfImportDeclaration(){

        for(FullIdent usedImport: this.usedImportsFullIndents){
            this.usedImports.add(new ImportDeclaration(
                    usedImport.getText().replaceAll("\\[.*]", ""),
                    false,
                    false
            ));
        }
    }

    public ArrayList<FullIdent> getDeclaredImportsFullIndent() {
        return declaredImportsFullIndent;
    }

    public ArrayList<ImportDeclaration> getDeclaredImports() {
        return declaredImports;
    }

    public ArrayList<FullIdent> getUsedImportsFullIndents() {
        return usedImportsFullIndents;
    }

    public ArrayList<ImportDeclaration> getUsedImports() {
        return usedImports;
    }

    public ArrayList<FullIdent> getUnUsedImportsFullIndents() {
        return unUsedImportsFullIndents;
    }

    public ArrayList<ImportDeclaration> getUnUsedImports() {
        return unUsedImports;
    }

    public ArrayList<Dependency> getUsedDependencies() {
        return usedDependencies;
    }


    private ArrayList<DetailAST> getIndividualAST(DetailAST root){
        if(root == null) return new ArrayList<>();

        ArrayList<DetailAST> children = new ArrayList();

        children.add(root);
        children.addAll(getIndividualAST(root.getFirstChild()));
        children.addAll(getIndividualAST(root.getNextSibling()));

        return children;
    }
}
