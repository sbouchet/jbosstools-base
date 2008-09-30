/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.common.el.internal.core.parser.token;

import org.jboss.tools.common.el.core.parser.ITokenDescription;
import org.jboss.tools.common.el.core.parser.Tokenizer;

/**
 * 
 * @author V. Kabanovich
 *
 */
public class PrimitiveValueTokenDescription implements ITokenDescription {
	public static final int PRIMITIVE_VALUE = 6;

	public static PrimitiveValueTokenDescription INSTANCE = new PrimitiveValueTokenDescription();

	private static final String[] OPS_2 = {
		"null", "true", "false",
	};
	public String getName() {
		return "Primitive";
	}

	public int getType() {
		return PRIMITIVE_VALUE;
	}

	public boolean isStart(Tokenizer tokenizer, int offset) {
		if(isNumber(tokenizer, offset)) {
			return true;
		}
		int end = -1;
		boolean canBeFollowedByOperand = true;
		for (int i = 0; end < 0 && i < OPS_2.length; i++) {
			if(tokenizer.startsWith(OPS_2[i])) {
				end = offset + OPS_2[i].length();
				canBeFollowedByOperand = false;
			}
		}
		if(end < 0) return false;
		char ch = tokenizer.lookUpChar(end);
		if(Character.isWhitespace(ch) || ch == '\0' || !Character.isJavaIdentifierPart(ch)
			|| (canBeFollowedByOperand && Character.isJavaIdentifierPart(ch))) {
			return true;
		}
		return false;
	}

	private boolean isNumber(Tokenizer tokenizer, int offset) {
		char ch = tokenizer.lookUpChar(offset);
		if(ch == '.') {
			ch = tokenizer.lookUpChar(offset + 1);
		}
		return ch != '\0' && Character.isDigit(ch);
	}

	public boolean read(Tokenizer tokenizer, int offset) {
		if(isNumber(tokenizer, offset)) {
			return readNumber(tokenizer, offset);
		}
		int end = -1;
		for (int i = 0; end < 0 && i < OPS_2.length; i++) {
			if(tokenizer.startsWith(OPS_2[i])) {
				end = offset + OPS_2[i].length();
			}
		}
		tokenizer.addToken(getType(), offset, end);
		return true;
	}

	static String TYPE_CHAR = "lLfFdD";
	private boolean readNumber(Tokenizer tokenizer, int offset) {
		int i = offset;
		int dotCount = 1;
		if(tokenizer.startsWith("0x")) {
			i += 2;
			dotCount = 0;
		}
		char ch = '\0';
		boolean lastCharIsWrong = false;
		while((ch = tokenizer.readNextChar()) != '\0') {
			if(ch == '.') {
				dotCount--;
				if(dotCount < 0) {
					lastCharIsWrong = true;
					break;
				}
			} else if(!Character.isDigit(ch)) {
				if(TYPE_CHAR.indexOf(ch) >= 0) {
					char ch1 = tokenizer.lookUpChar(i + 1);
					if(ch1 == '\0' || !Character.isJavaIdentifierPart(ch1)) {
						i++;
						break;
					}
				}
				lastCharIsWrong = true;
				break;
			}
			i++;
			
		}
		if(lastCharIsWrong) {
			tokenizer.releaseChar();
		}
		tokenizer.addToken(getType(), offset, i);
		return true;
	}

}
