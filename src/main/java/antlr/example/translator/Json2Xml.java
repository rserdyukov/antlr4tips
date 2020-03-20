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

public class Json2Xml {
    public static class XMLEmitter extends JsonBaseListener {
        ParseTreeProperty<String> xml = new ParseTreeProperty<String>();
        String getXML(ParseTree ctx) { return xml.get(ctx); }
        void setXML(ParseTree ctx, String s) { xml.put(ctx, s); }

        public void exitJson(JsonParser.JsonContext ctx) {
            setXML(ctx, getXML(ctx.getChild(0)));
        }

        public void exitAnObject(JsonParser.AnObjectContext ctx) {
            StringBuilder buf = new StringBuilder();
            buf.append("\n");
            for (JsonParser.PairContext pctx : ctx.pair()) {
                buf.append(getXML(pctx));
            }
            setXML(ctx, buf.toString());
        }
        public void exitEmptyObject(JsonParser.EmptyObjectContext ctx) {
            setXML(ctx, "");
        }

        public void exitArrayOfValues(JsonParser.ArrayOfValuesContext ctx) {
            StringBuilder buf = new StringBuilder();
            buf.append("\n");
            for (JsonParser.ValueContext vctx : ctx.value()) {
                buf.append("<element>"); // conjure up element for valid XML
                buf.append(getXML(vctx));
                buf.append("</element>");
                buf.append("\n");
            }
            setXML(ctx, buf.toString());
        }

        public void exitEmptyArray(JsonParser.EmptyArrayContext ctx) {
            setXML(ctx, "");
        }

        public void exitPair(JsonParser.PairContext ctx) {
            String tag = stripQuotes(ctx.STRING().getText());
            JsonParser.ValueContext vctx = ctx.value();
            String x = String.format("<%s>%s</%s>\n", tag, getXML(vctx), tag);
            setXML(ctx, x);
        }

        public void exitObjectValue(JsonParser.ObjectValueContext ctx) {
            // analogous to String value() {return object();}
            setXML(ctx, getXML(ctx.object()));
        }

        public void exitArrayValue(JsonParser.ArrayValueContext ctx) {
            setXML(ctx, getXML(ctx.array())); // String value() {return array();}
        }

        public void exitAtom(JsonParser.AtomContext ctx) {
            setXML(ctx, ctx.getText());
        }

        public void exitString(JsonParser.StringContext ctx) {
            setXML(ctx, stripQuotes(ctx.getText()));
        }

        public static String stripQuotes(String s) {
            if ( s==null || s.charAt(0)!='"' ) return s;
            return s.substring(1, s.length() - 1);
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
//        System.out.println(tree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();
        XMLEmitter converter = new XMLEmitter();
        walker.walk(converter, tree);
        System.out.println(converter.getXML(tree));
    }
}
