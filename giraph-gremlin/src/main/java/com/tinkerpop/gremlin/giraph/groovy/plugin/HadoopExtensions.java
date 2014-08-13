package com.tinkerpop.gremlin.giraph.groovy.plugin;

import com.tinkerpop.gremlin.giraph.hdfs.HDFSTools;
import com.tinkerpop.gremlin.giraph.hdfs.HiddenFileFilter;
import com.tinkerpop.gremlin.giraph.hdfs.KryoWritableIterator;
import com.tinkerpop.gremlin.giraph.hdfs.TextIterator;
import com.tinkerpop.gremlin.giraph.process.computer.util.KryoWritable;
import com.tinkerpop.gremlin.util.StreamFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by jason on 8/12/14.
 */
public
class HadoopExtensions {
    public static
    String toString(FileStatus self) {
        StringBuilder s = new StringBuilder();
        s.append(self.getPermission()).append(' ');
        s.append(self.getOwner()).append(' ');
        s.append(self.getGroup()).append(' ');
        s.append(self.getLen()).append(' ');
        if (self.isDir())
            s.append("(D) ");
        s.append(self.getPath().getName());
        return s.toString();
    }

    public static
    List<String> ls(FileSystem self, String path) throws IOException {
        if (null == path || path.equals("/"))
            path = self.getHomeDirectory().toString();
        return Stream.of(self.globStatus(new Path(path + "/*"))).map(Object::toString).collect(Collectors.toList());
    }

    public static
    boolean cp(FileSystem self, String from, String to) throws IOException {
        return FileUtil.copy(self, new Path(from), self, new Path(to), false, new Configuration());
    }

    public static
    boolean exists(FileSystem self, String path) throws IOException {
        return self.exists(new Path(path));
    }

    public static
    boolean rm(FileSystem self, String path) throws IOException {
        return HDFSTools.globDelete(self, path, false);
    }

    public static
    boolean rmr(FileSystem self, String path) throws IOException {
        return HDFSTools.globDelete(self, path, true);
    }

    public static
    void copyToLocal(FileSystem self, String from, String to) throws IOException {
        self.copyToLocalFile(new Path(from), new Path(to));
    }

    public static
    void copyFromLocal(FileSystem self, String from, String to) throws IOException {
        self.copyFromLocalFile(new Path(from), new Path(to));
    }

    public static
    void mergeToLocal(FileSystem self, String from, String to) throws IOException{

        final FileSystem fs = self;
        final FileSystem local = FileSystem.getLocal(new Configuration());
        final FSDataOutputStream outA = local.create(new Path(to));

        HDFSTools.getAllFilePaths(fs, new Path(from), HiddenFileFilter.instance()).stream().forEach((p) -> {
            try (FSDataInputStream inA = fs.open(p)) {
                IOUtils.copyBytes(inA, outA, 8192);
                inA.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        outA.close();
    }

    public static
    <I> Iterator<I> head(FileSystem self, final String path, final long totalLines) throws IOException {
        return head(self, path, totalLines, Text.class);
    }

    public static
    <I> Iterator<I> head(FileSystem self, final String path) throws IOException {
        return head(self, path, Long.MAX_VALUE, Text.class);
    }

    public static
    <I> Iterator<I> head(FileSystem self, String path, Class<? extends Writable> writableClass) throws IOException{
        return head(self, path, Long.MAX_VALUE, writableClass);
    }

    public static
    <I> Iterator<I> head(FileSystem self,
                         final String path,
                         final long totalKeyValues,
                         final Class<? extends Writable> writableClass) throws IOException {

        // if(writableClass.equals(org.apache.giraph.graph.Vertex.class)) {
        /// return StreamFactory.stream(new GiraphVertexIterator(((FileSystem) delegate).getConf(), new Path(path))).limit(totalKeyValues).iterator();
        // } else
        if (writableClass.equals(KryoWritable.class)) {
            return (Iterator<I>) StreamFactory.stream(new KryoWritableIterator(self.getConf(),
                                                                               new Path(path)))
                                              .limit(totalKeyValues)
                                              .iterator();
        }
        else {
            return (Iterator<I>) StreamFactory.stream(new TextIterator(self.getConf(), new Path(path)))
                                              .limit(totalKeyValues)
                                              .iterator();
        }
    }

        /*FileSystem.metaClass.unzip = { final String from, final String to, final boolean deleteZip ->
            HDFSTools.decompressPath((FileSystem) delegate, from, to, Tokens.BZ2, deleteZip);
        }*/


}
