package com.Utils;

import com.Components.UnusedImportsCheck;
import com.github.javaparser.ast.ImportDeclaration;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;

import java.io.File;
import java.util.ArrayList;


public class ProjectFile extends File {

    private ArrayList<FullIdent> declaredImportsFullIndent = new ArrayList<>();
    private ArrayList<ImportDeclaration> declaredImports = new ArrayList<>();

    private ArrayList<FullIdent> usedImportsFullIndents = new ArrayList<>();
    private ArrayList<ImportDeclaration> usedImports = new ArrayList<>();

    private ArrayList<FullIdent> unUsedImportsFullIndents = new ArrayList<>();
    private ArrayList<ImportDeclaration> unUsedImports = new ArrayList<>();

    private DetailAST projectAST;


    /***The ProjectFile class extends the regular file class to have additional functionalities which are relevant to the
     * dependency usage analysis by converting the .java file to its DetailAST representation
     * @param pathname
     */

    public ProjectFile(String pathname){
        super(pathname);
        try{
//            System.out.println("Trying to parse: " + this.getAbsolutePath());
            this.projectAST = JavaParser.parseFile(this.getAbsoluteFile(), JavaParser.Options.WITHOUT_COMMENTS);
            this.classifyImports();
            this.convertSetOfFullIndentToArrayListOfImportDeclaration();
//            this.usedImports.forEach(imp -> System.out.println(imp));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /***Simplification of initialization of the ProjectFile class
     * @param file .java file
     * @return projectFile instance
     */

    public static ProjectFile initializeProjectFile(File file){
        try{
            return new ProjectFile(file.getAbsolutePath());
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /***getter method of DetailAST representation of ProjectFile
     * @return
     */
    public DetailAST getProjectAST() {
        return projectAST;
    }

    /***getter method of used imports of the ProjectFile
     * @return list of usedImports
     */
    public ArrayList<ImportDeclaration> getUsedImports() {
        return usedImports;
    }

    /***getter method of unused imports of the ProjectFile
     * @return list of UnUsedImports
     */
    public ArrayList<ImportDeclaration> getUnUsedImports() {
        return unUsedImports;
    }

    /***each import statement of the ProjectFile is classified in either a UsedImport or UnusedImport by traversing the
     * DetailAST representation and passing each branch and leaf into the Checkstyle UnusedImportsCheck
     */
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

    /***Checkstyle uses the FullIndent class which contains the row of where the import is declared, to avoid duplicates,
     * each FullIndent is converted into a ImportDeclaration and only added if the import has not already been added.
     */

    private void convertSetOfFullIndentToArrayListOfImportDeclaration(){

        for(FullIdent usedImport: this.usedImportsFullIndents){
            ImportDeclaration currImport = new ImportDeclaration(
                    usedImport.getText().replaceAll("\\[.*]", ""),
                    false,
                    false
            );

            if(!this.usedImports.contains(currImport)){
                this.usedImports.add(currImport);
            }
        }
    }

    /***to check which imports are used in the source code, a list of the indivual nodes have to be passed to the
     * Checkstyle class UnusedImportsCheck. By recursively calling {@link #getIndividualAST(DetailAST) getIndividualAST}
     * on the root's children and siblings.
     * @param root given a .java file converted into its DetailAST representation
     * @return list of the individual nodes
     */

    private ArrayList<DetailAST> getIndividualAST(DetailAST root){
        if(root == null) return new ArrayList<>();

        ArrayList<DetailAST> children = new ArrayList();

        children.add(root);
        children.addAll(getIndividualAST(root.getFirstChild()));
        children.addAll(getIndividualAST(root.getNextSibling()));

        return children;
    }
}
