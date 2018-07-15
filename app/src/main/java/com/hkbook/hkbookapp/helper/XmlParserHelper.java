package com.hkbook.hkbookapp.helper;

import android.util.Log;
import android.util.Xml;

import com.hkbook.hkbookapp.Author;
import com.hkbook.hkbookapp.Book;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lekha on 6/17/2018.
 */

public class XmlParserHelper {
    private static final String ns = null;

    public Book parseBook(InputStream in) throws XmlPullParserException, IOException {
        Book book = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            //
            parser.require(XmlPullParser.START_TAG, ns, "GoodreadsResponse");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // Starts by looking for the entry tag
                if (name.equals("book")) {
                    book = readBook(parser);
                } else {
                    skip(parser);
                }
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            book = null;
        } finally {
            in.close();
        }
        return book;
    }

    private Book readBook(XmlPullParser parser) throws XmlPullParserException, IOException {
        Book ret = new Book();
        int numOfFields = 6;
        int count = 0;

        parser.require(XmlPullParser.START_TAG, ns, "book");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "title":
                    String title = readTag(parser, "title");
                    ret.setTitle(title);
                    count++;
                    break;
                case "isbn13":
                    String isbn = readTag(parser, "isbn13");
                    ret.setIsbn(isbn);
                    count++;
                    break;
                case "image_url":
                    String imageUrl = readTag(parser, "image_url");
                    imageUrl = getLargeImageUrl(imageUrl);
                    ret.setImageUrl(imageUrl);
                    count++;
                    break;
                case "authors":
                    List<Author> authors = readAuthors(parser);
                    if (authors.size() > 0) {
                        ret.setAuthor(authors.get(0));
                    }
                    count++;
                    break;
                case "description":
                    String description = readTag(parser, "description");
                    ret.setDescription(description);
                    count++;
                    break;
                case "url":
                    String goodreadsUrl = readTag(parser, "url");
                    ret.setGoodreadsUrl(goodreadsUrl);
                    count++;
                    break;
                default:
                    skip(parser);
            }
            if (count >= numOfFields) {
                return ret;
            }
        }
        return ret;
    }

    // Processes title tags in the feed.
    private String readTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String result = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return result;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private List<Author> readAuthors(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Author> ret = new ArrayList<>();
        parser.require(XmlPullParser.START_TAG, ns, "authors");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("author")) {
                Author author = readAuthor(parser);
                ret.add(author);
            } else {
                skip(parser);
            }
        }
        return ret;
    }

    private Author readAuthor(XmlPullParser parser) throws IOException, XmlPullParserException {
        Author ret = new Author();
        parser.require(XmlPullParser.START_TAG, ns, "author");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("name")) {
                String authorName = readTag(parser, "name");
                ret.setName(authorName);
            } else {
                skip(parser);
            }
        }
        return ret;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private String getLargeImageUrl(String imageUrl) {
        StringBuilder largeUrl = new StringBuilder(imageUrl);
        int idx = largeUrl.lastIndexOf("m");
        largeUrl.setCharAt(idx, 'l');
        return largeUrl.toString();
    }
}
