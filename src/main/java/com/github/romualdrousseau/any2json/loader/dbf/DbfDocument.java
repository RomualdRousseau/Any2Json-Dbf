package com.github.romualdrousseau.any2json.loader.dbf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.base.BaseDocument;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.util.Disk;
import com.linuxense.javadbf.DBFReader;

public class DbfDocument extends BaseDocument {

    private static final List<String> EXTENSIONS = List.of(".dbf");
    private static final EnumSet<Hint> CAPABILITIES = EnumSet.of(Document.Hint.INTELLI_TAG);

    private DbfSheet sheet;

    @Override
    protected EnumSet<Hint> getIntelliCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public boolean open(final File dbfFile, final String encoding, final String password) {
        if (dbfFile == null) {
            throw new IllegalArgumentException();
        }

        this.sheet = null;

        if (EXTENSIONS.stream().filter(x -> dbfFile.getName().toLowerCase().endsWith(x)).findAny().isEmpty()) {
            return false;
        }

        if (encoding != null && this.openWithEncoding(dbfFile, encoding)) {
            return true;
        } else if (this.openWithEncoding(dbfFile, "ISO-8859-1")) {
            return true;
        } else {
            this.close();
            return false;
        }
    }

    @Override
    public void close() {
        try {
            if (this.sheet != null) {
                this.sheet.close();
                this.sheet = null;
            }
        } catch (final IOException x) {
            // throw new UncheckedIOException(x);
        } finally {
            super.close();
        }
    }

    @Override
    public int getNumberOfSheets() {
        return 1;
    }

    @Override
    public String getSheetNameAt(final int i) {
        return this.sheet.getName();
    }

    @Override
    public Sheet getSheetAt(final int i) {
        return new BaseSheet(this, this.sheet.getName(), this.sheet.ensureDataLoaded());
    }

    @Override
    public void autoRecipe(final BaseSheet sheet) {
    }

    private boolean openWithEncoding(final File dbfFile, final String encoding) {
        try {
            final var reader = new DBFReader(new FileInputStream(dbfFile), Charset.forName(encoding));
            final var sheetName = Disk.removeExtension(dbfFile.getName());
            this.sheet = new DbfSheet(sheetName, reader);
            return true;
        } catch (final IOException | UnsupportedCharsetException x) {
            return false;
        }
    }
}
