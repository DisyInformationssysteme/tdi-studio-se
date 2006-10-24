// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.designer.runprocess.language.perl;

import org.talend.core.model.temp.ECodeLanguage;
import org.talend.designer.core.language.perl.ICodeSyntaxChecker;


/**
 * DOC amaumont  class global comment. Detailled comment
 * <br/>
 *
 * $Id$
 *
 */
public class SyntaxCheckerFactory {

    private static SyntaxCheckerFactory instance;

    public static SyntaxCheckerFactory getInstance() {
        if (instance == null) {
            instance = new SyntaxCheckerFactory();
        }
        return instance;
    }
    
    public ICodeSyntaxChecker getSyntaxChecker(ECodeLanguage codeLanguage) {
        if (codeLanguage == ECodeLanguage.PERL) {
            return (ICodeSyntaxChecker) new PerlExpressionSyntaxChecker();
//        } else if (codeLanguage == ECodeLanguage.JAVA) {
//            return null;
        } else {
            throw new IllegalArgumentException("Syntax checker not found for this language: " + codeLanguage.getName());
        }
    }
    
}
