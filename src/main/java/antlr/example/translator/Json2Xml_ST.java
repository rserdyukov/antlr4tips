package antlr.example.translator; /***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
***/
import antlr.example.JsonBaseListener;
import antlr.example.JsonLexer;
import antlr.example.JsonParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.io.FileInputStream;
import java.io.InputStream;

/*
{
    "description" : "An imaginary server config file",
    "logs" : {"level":"verbose", "dir":"/var/log"},
    "host" : "antlr.org",
    "admin": ["parrt", "tombu"]
    "aliases": []
}

to

<description>An imaginary server config file</description>
<logs>
    <level>verbose</level>
    <dir>/var/log</dir>
</logs>
<host>antlr.org</host>
<admin>
    <element>parrt</element> <!-- inexact -->
    <element>tombu</element>
</admin>
<aliases></aliases>
 */

public class Json2Xml_ST {
    public static class XMLEmitter extends JsonBaseListener {
        ParseTreeProperty<ST> xml = new ParseTreeProperty<ST>();
        STGroup templates = new STGroupFile("XML.stg");

        @Override
        public void exitJson(JsonParser.JsonContext ctx) {
            xml.put(ctx, xml.get(ctx.getChild(0)));
        }

        @Override
        public void exitEmptyObject(JsonParser.EmptyObjectContext ctx) {
            xml.put(ctx, templates.getInstanceOf("empty"));
        }

        @Override
        public void exitAnObject(JsonParser.AnObjectContext ctx) {
            ST objectST = templates.getInstanceOf("object");
            for (JsonParser.PairContext pctx : ctx.pair()) {
                objectST.add("fields", xml.get(pctx));
            }
            xml.put(ctx, objectST);
        }

        @Override
        public void exitEmptyArray(JsonParser.EmptyArrayContext ctx) {
            xml.put(ctx, templates.getInstanceOf("empty"));
        }

        @Override
        public void exitArrayOfValues(JsonParser.ArrayOfValuesContext ctx) {
            ST arrayST = templates.getInstanceOf("array");
            for (JsonParser.ValueContext vctx : ctx.value()) {
                arrayST.add("values", xml.get(vctx));
            }
            xml.put(ctx, arrayST);
        }

        @Override
        public void exitPair(JsonParser.PairContext ctx) {
            String name = stripQuotes(ctx.STRING().getText());
            JsonParser.ValueContext vctx = ctx.value();
            ST tag = templates.getInstanceOf("tag");
            tag.add("name", name);
            tag.add("content", xml.get(vctx));
            xml.put(ctx, tag);
        }

        @Override
        public void exitAtom(JsonParser.AtomContext ctx) {
            xml.put(ctx, value(ctx.start.getText()));
        }

        @Override
        public void exitObjectValue(JsonParser.ObjectValueContext ctx) {
            xml.put(ctx, xml.get(ctx.object()));
        }

        @Override
        public void exitArrayValue(JsonParser.ArrayValueContext ctx) {
            JsonParser.ArrayContext array = ctx.array();
            xml.put(ctx, xml.get(array));
        }

        @Override
        public void exitString(JsonParser.StringContext ctx) {
            xml.put(ctx, value(stripQuotes(ctx.start.getText())));
        }

        ST value(Object x) {
            ST st = templates.getInstanceOf("value");
            st.add("x", x);
            return st;
        }

        public static String stripQuotes(String s) {
            if ( s==null || s.charAt(0)!='"' ) return s;
            return s.substring(1, s.length()-1);
        }
    }

    public static void main(String[] args) throws Exception {
        String inputFile = null;
        if ( args.length>0 ) inputFile = args[0];
        InputStream is = System.in;
        if ( inputFile!=null ) {
            is = new FileInputStream(inputFile);
        }
        CharStream input = CharStreams.fromStream(is);
        JsonLexer lexer = new JsonLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JsonParser parser = new JsonParser(tokens);
        parser.setBuildParseTree(true);
        ParseTree tree = parser.json();
        // show tree in text form
        System.out.println(tree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();
        XMLEmitter converter = new XMLEmitter();
        walker.walk(converter, tree);
        System.out.println(converter.xml.get(tree).render());
    }
}
