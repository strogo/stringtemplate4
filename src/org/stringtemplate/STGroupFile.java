/*
 [The "BSD licence"]
 Copyright (c) 2009 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.stringtemplate;

import org.stringtemplate.compiler.CompiledST;
import org.stringtemplate.misc.ErrorManager;
import org.stringtemplate.misc.Misc;

import java.io.File;
import java.net.URL;

/** The internal representation of a single group file (which must end in
 *  ".stg").  If we fail to find a group file, look for it via the
 *  CLASSPATH as a resource.
 */
public class STGroupFile extends STGroup {
    public String fileName;
    public URL url;

    protected boolean alreadyLoaded = false;
        
    /** Load a file relative to current dir or from root or via CLASSPATH. */
    public STGroupFile(String fileName) {
        if ( !fileName.endsWith(".stg") ) {
            throw new IllegalArgumentException("Group file names must end in .stg: "+fileName);
        }
        try {
            File dir = new File(fileName);
            if ( dir.exists() ) {
                url = dir.toURI().toURL();
            }
            else { // try in classpath
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                URL groupFileURL = cl.getResource(fileName);
                if ( groupFileURL==null ) {
                    cl = this.getClass().getClassLoader();
                    groupFileURL = cl.getResource(fileName);
                }
                if ( groupFileURL==null ) {
                    throw new IllegalArgumentException("No such group file: "+
                                                       fileName);
                }
            }
        }
        catch (Exception e) {
            ErrorManager.internalError(null, "can't load group file "+fileName, e);
        }
        this.fileName = fileName;
    }

    public STGroupFile(String fullyQualifiedFileName, String encoding) {
        this(fullyQualifiedFileName);
        this.encoding = encoding;
    }

    protected CompiledST load(String name) {
        String prefix = new File(name).getParent();
        if ( !prefix.endsWith("/") ) prefix += "/";
        _load(prefix);
        return templates.get(name);
    }

    public void load() { _load("/"); }

    protected void _load(String prefix) {
        if ( alreadyLoaded ) return;
        loadGroupFile(prefix, url.toString());
        alreadyLoaded = true;
    }

    public String show() {
        if ( !alreadyLoaded ) load();
        return super.show();
    }

    public String getName() { return Misc.getFileNameNoSuffix(fileName); }
}
