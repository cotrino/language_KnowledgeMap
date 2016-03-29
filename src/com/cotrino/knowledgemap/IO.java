/**
 *  Copyright (C) 2016 José Miguel Cotrino Benavides
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cotrino.knowledgemap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Utility class to interact with user through the command line.
 * @author cotrino
 *
 */
public class IO {

	public static String getLine() {

		String userInput = "";
		try {
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			userInput = bufferRead.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println("User said: '" + userInput + "'");
		return userInput;

	}
	
	public static boolean askConfirmation(String question) {

		Boolean ans = null;
		while (ans == null) {
			System.out.print(question+" [y/n] ");
			String answer = getLine();
			if (answer.equals("y")) {
				ans = true;
			} else if (answer.equals("n")) {
				ans = false;
			} else {
				System.err.println("I have not understood you. Please answer 'yes' [y] or 'no' [n].");
			}
		}
		return ans;

	}
	
}
