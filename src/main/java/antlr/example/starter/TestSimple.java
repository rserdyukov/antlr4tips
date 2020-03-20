package antlr.example.starter; /***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
***/
import antlr.example.SimpleLexer;
import antlr.example.SimpleParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Tree;

import java.io.IOException;

public class TestSimple {
	public static void main(String[] args) throws IOException {
		CharStream input = CharStreams.fromFileName(args[0]);
		SimpleLexer lexer = new SimpleLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SimpleParser parser = new SimpleParser(tokens);
		ParseTree t = parser.prog();
		System.out.println(t.toStringTree(parser));
	}
}
