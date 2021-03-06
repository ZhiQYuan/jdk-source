/*
 * Copyright (c) 1998, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.sun.tools.doclets.formats.html;

import java.io.*;
import java.util.*;

import com.sun.javadoc.*;
import com.sun.tools.doclets.formats.html.markup.*;
import com.sun.tools.doclets.internal.toolkit.*;
import com.sun.tools.doclets.internal.toolkit.util.*;

/**
 * Abstract class to print the class hierarchy page for all the Classes. This
 * is sub-classed by {@link PackageTreeWriter} and {@link TreeWriter} to
 * generate the Package Tree and global Tree(for all the classes and packages)
 * pages.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 *
 * @author Atul M Dambalkar
 */
@Deprecated
public abstract class AbstractTreeWriter extends HtmlDocletWriter {

    /**
     * The class and interface tree built by using {@link ClassTree}
     */
    protected final ClassTree classtree;

    /**
     * Constructor initializes classtree variable. This constructor will be used
     * while generating global tree file "overview-tree.html".
     *
     * @param configuration  The current configuration
     * @param filename   File to be generated.
     * @param classtree  Tree built by {@link ClassTree}.
     * @throws IOException
     * @throws DocletAbortException
     */
    protected AbstractTreeWriter(ConfigurationImpl configuration,
                                 DocPath filename, ClassTree classtree)
                                 throws IOException {
        super(configuration, filename);
        this.classtree = classtree;
    }

    /**
     * Add each level of the class tree. For each sub-class or
     * sub-interface indents the next level information.
     * Recurses itself to add subclasses info.
     *
     * @param parent the superclass or superinterface of the list
     * @param list list of the sub-classes at this level
     * @param isEnum true if we are generating a tree for enums
     * @param contentTree the content tree to which the level information will be added
     */
    protected void addLevelInfo(ClassDoc parent, Collection<ClassDoc> list,
            boolean isEnum, Content contentTree) {
        if (!list.isEmpty()) {
            Content ul = new HtmlTree(HtmlTag.UL);
            for (ClassDoc local : list) {
                HtmlTree li = new HtmlTree(HtmlTag.LI);
                li.addStyle(HtmlStyle.circle);
                addPartialInfo(local, li);
                addExtendsImplements(parent, local, li);
                addLevelInfo(local, classtree.subs(local, isEnum),
                             isEnum, li);   // Recurse
                ul.addContent(li);
            }
            contentTree.addContent(ul);
        }
    }

    /**
     * Add the heading for the tree depending upon tree type if it's a
     * Class Tree or Interface tree.
     *
     * @param list List of classes which are at the most base level, all the
     * other classes in this run will derive from these classes
     * @param heading heading for the tree
     * @param div the content tree to which the tree will be added
     */
    protected void addTree(SortedSet<ClassDoc> list, String heading, HtmlTree div) {
        if (!list.isEmpty()) {
            ClassDoc firstClassDoc = list.first();
            Content headingContent = getResource(heading);
            Content sectionHeading = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING, true,
                    headingContent);
            HtmlTree htmlTree;
            if (configuration.allowTag(HtmlTag.SECTION)) {
                htmlTree = HtmlTree.SECTION(sectionHeading);
            } else {
                div.addContent(sectionHeading);
                htmlTree = div;
            }
            addLevelInfo(!firstClassDoc.isInterface()? firstClassDoc : null,
                    list, list == classtree.baseEnums(), htmlTree);
            if (configuration.allowTag(HtmlTag.SECTION)) {
                div.addContent(htmlTree);
            }
        }
    }

    /**
     * Add information regarding the classes which this class extends or
     * implements.
     *
     * @param parent the parent class of the class being documented
     * @param cd the classdoc under consideration
     * @param contentTree the content tree to which the information will be added
     */
    protected void addExtendsImplements(ClassDoc parent, ClassDoc cd,
            Content contentTree) {
        ClassDoc[] interfaces = cd.interfaces();
        if (interfaces.length > (cd.isInterface()? 1 : 0)) {
            Arrays.sort(interfaces);
            int counter = 0;
            for (ClassDoc intf : interfaces) {
                if (parent != intf) {
                    if (!(intf.isPublic() ||
                          utils.isLinkable(intf, configuration))) {
                        continue;
                    }
                    if (counter == 0) {
                        if (cd.isInterface()) {
                            contentTree.addContent(" (");
                            contentTree.addContent(getResource("doclet.also"));
                            contentTree.addContent(" extends ");
                        } else {
                            contentTree.addContent(" (implements ");
                        }
                    } else {
                        contentTree.addContent(", ");
                    }
                    addPreQualifiedClassLink(LinkInfoImpl.Kind.TREE,
                                             intf, contentTree);
                    counter++;
                }
            }
            if (counter > 0) {
                contentTree.addContent(")");
            }
        }
    }

    /**
     * Add information about the class kind, if it's a "class" or "interface".
     *
     * @param cd the class being documented
     * @param contentTree the content tree to which the information will be added
     */
    protected void addPartialInfo(ClassDoc cd, Content contentTree) {
        addPreQualifiedStrongClassLink(LinkInfoImpl.Kind.TREE, cd, contentTree);
    }

    /**
     * Get the tree label for the navigation bar.
     *
     * @return a content tree for the tree label
     */
    protected Content getNavLinkTree() {
        Content li = HtmlTree.LI(HtmlStyle.navBarCell1Rev, treeLabel);
        return li;
    }
}
