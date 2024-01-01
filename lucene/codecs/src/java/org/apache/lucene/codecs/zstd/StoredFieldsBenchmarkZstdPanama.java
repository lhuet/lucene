package org.apache.lucene.codecs.zstd;

import org.apache.lucene.codecs.lucene99.Lucene99Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

public class StoredFieldsBenchmarkZstdPanama {

    public static void main(String args[]) throws Exception {

        //System.setProperty("java.library.path", "/usr/local/Cellar/zstd/1.5.2/lib");

        if (args.length != 4) {
            System.err.println("Usage: StoredFieldsBenchmark /path/to/geonames.txt /path/to/index/dir (BEST_SPEED|BEST_COMPRESSION) doc_limit(or -1 means index all lines)");
            System.exit(1);
        }

        String geonamesDataPath = args[0];
        String indexPath = args[1];
//        Lucene94Codec.Mode mode;
//        switch (args[2]) {
//            case "BEST_SPEED":
//                mode = Lucene94Codec.Mode.BEST_SPEED;
//                break;
//            case "BEST_COMPRESSION":
//                mode = Lucene94Codec.Mode.BEST_COMPRESSION;
//                break;
//            default:
//                throw new AssertionError();
//        }
        String mode = args[2];
        //int compressionLevel = Integer.parseInt(args[2]);
        int docLimit = Integer.parseInt(args[3]);

        IOUtils.rm(Paths.get(indexPath));
        try (FSDirectory dir = FSDirectory.open(Paths.get(indexPath))) {

            System.err.println("Warm up indexing");
            try (IndexWriter iw = new IndexWriter(dir, getConfig(mode));
                 LineNumberReader reader = new LineNumberReader(new InputStreamReader(Files.newInputStream(Paths.get(geonamesDataPath))))) {
                indexDocs(iw, reader, docLimit);
            }

            System.err.println("Now run indexing");
            try (IndexWriter iw = new IndexWriter(dir, getConfig(mode));
                 LineNumberReader reader = new LineNumberReader(new InputStreamReader(Files.newInputStream(Paths.get(geonamesDataPath))))) {
                long t0 = System.nanoTime();
                indexDocs(iw, reader, docLimit);
                System.out.println(String.format(Locale.ROOT, "Indexing time: %d msec", (System.nanoTime() - t0) / 1_000_000));
            }

            long storeSizeBytes = 0;
            for (String f : dir.listAll()) {
                storeSizeBytes += dir.fileLength(f);
            }
            System.out.println(String.format(Locale.ROOT, "Stored fields size: %.3f MB", storeSizeBytes / 1024. / 1024.));

            try (DirectoryReader reader = DirectoryReader.open(dir)) {
                System.err.println("Warm up searching");
                getDocs(reader);

                System.err.println("Now run searching");
                // Take the min across multiple runs to decrease noise
                long minDurationNS = Long.MAX_VALUE;
                for (int i = 0; i < 10; ++i) {
                    long t0 = System.nanoTime();
                    getDocs(reader);
                    minDurationNS = Math.min(minDurationNS, System.nanoTime() - t0);
                }
                System.out.println(String.format(Locale.ROOT, "Retrieved time: %.5f msec", minDurationNS / 1_000_000.));
            }
        }
    }

    private static IndexWriterConfig getConfig(String mode) {

        IndexWriterConfig iwc = new IndexWriterConfig();
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        switch (mode) {
            case "BEST_SPEED":
                iwc.setCodec(new Lucene99Codec(Lucene99Codec.Mode.BEST_SPEED));
                break;
            case "BEST_COMPRESSION":
                iwc.setCodec(new Lucene99Codec(Lucene99Codec.Mode.BEST_COMPRESSION));
                break;
            case "ZSTD1dict":
                iwc.setCodec(new ZstdDict90Codec(1));
                break;
            case "ZSTD2dict":
                iwc.setCodec(new ZstdDict90Codec(2));
                break;
            case "ZSTD5dict":
                iwc.setCodec(new ZstdDict90Codec(5));
                break;
            case "ZSTD10dict":
                iwc.setCodec(new ZstdDict90Codec(10));
                break;
            case "ZSTD15dict":
                iwc.setCodec(new ZstdDict90Codec(15));
                break;
            case "ZSTD20dict":
                iwc.setCodec(new ZstdDict90Codec(20));
                break;
            case "ZSTD1":
                iwc.setCodec(new Zstd90Codec(1));
                break;
            case "ZSTD2":
                iwc.setCodec(new Zstd90Codec(2));
                break;
            case "ZSTD5":
                iwc.setCodec(new Zstd90Codec(5));
                break;
            case "ZSTD10":
                iwc.setCodec(new Zstd90Codec(10));
                break;
            case "ZSTD15":
                iwc.setCodec(new Zstd90Codec(15));
                break;
            case "ZSTD20":
                iwc.setCodec(new Zstd90Codec(20));
                break;
            default:
                throw new AssertionError();
        }
        //iwc.setCodec(new Lucene94Codec(mode));
        //iwc.setCodec(new Zstd90Codec(compressionLevel));
        iwc.setMergeScheduler(new SerialMergeScheduler());
        // provoke much segments, lots of compress/deompress/bulk copy:
        iwc.setMaxBufferedDocs(100);
        iwc.setRAMBufferSizeMB(IndexWriterConfig.DISABLE_AUTO_FLUSH);
        return iwc;
    }

    static void indexDocs(IndexWriter iw, LineNumberReader reader, int docLimit) throws Exception {
        Document doc = new Document();
        Field fields[] = new Field[19];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new StoredField("field " + i, "");
            doc.add(fields[i]);
        }

        String line = null;
        while ((line = reader.readLine()) != null) {
            if (reader.getLineNumber() % 10000 == 0) {
                System.err.println("doc: " + reader.getLineNumber());
            }
            if (docLimit != -1 && reader.getLineNumber() == docLimit) {
                break;
            }
            String values[] = line.split("\t");
            if (values.length != fields.length) {
                throw new RuntimeException("bogus: " + values);
            }
            for (int i = 0; i < values.length; i++) {
                fields[i].setStringValue(values[i]);
            }
            iw.addDocument(doc);
        }
        iw.flush();
    }

    static int DUMMY;

    static void getDocs(IndexReader reader) throws IOException {
        int docId = 42;
        for (int i = 0; i < 10_000; ++i) {
            Document doc = reader.storedFields().document(docId);
            DUMMY += doc.getFields().size(); // Prevent the JVM from optimizing away the read of the stored document
            docId = (docId + 65535) % reader.maxDoc();
        }
    }

}
