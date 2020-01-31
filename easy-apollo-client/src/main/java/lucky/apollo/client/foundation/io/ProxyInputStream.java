package lucky.apollo.client.foundation.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author luckylau
 * @Date 2019/12/17
 */
public class ProxyInputStream extends FilterInputStream {

    public static final int EOF = -1;

    /**
     * Creates a <code>FilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param in the underlying input stream, or <code>null</code> if
     *           this instance is to be created without an underlying stream.
     */
    protected ProxyInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        try {
            beforeRead(1);
            final int b = in.read();
            afterRead(b != EOF ? 1 : EOF);
            return b;
        } catch (final IOException e) {
            handleIOException(e);
            return EOF;
        }
    }

    /**
     * Invokes the delegate's <code>read(byte[])</code> method.
     *
     * @param bts the buffer to read the bytes into
     * @return the number of bytes read or EOF if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read(final byte[] bts) throws IOException {
        try {
            beforeRead(bts != null ? bts.length : 0);
            final int n = in.read(bts);
            afterRead(n);
            return n;
        } catch (final IOException e) {
            handleIOException(e);
            return EOF;
        }
    }

    /**
     * Invokes the delegate's <code>read(byte[], int, int)</code> method.
     *
     * @param bts the buffer to read the bytes into
     * @param off The start offset
     * @param len The number of bytes to read
     * @return the number of bytes read or -1 if the end of stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read(final byte[] bts, final int off, final int len) throws IOException {
        try {
            beforeRead(len);
            final int n = in.read(bts, off, len);
            afterRead(n);
            return n;
        } catch (final IOException e) {
            handleIOException(e);
            return EOF;
        }
    }

    /**
     * Invokes the delegate's <code>skip(long)</code> method.
     *
     * @param ln the number of bytes to skip
     * @return the actual number of bytes skipped
     * @throws IOException if an I/O error occurs
     */
    @Override
    public long skip(final long ln) throws IOException {
        try {
            return in.skip(ln);
        } catch (final IOException e) {
            handleIOException(e);
            return 0;
        }
    }

    /**
     * Invokes the delegate's <code>available()</code> method.
     *
     * @return the number of available bytes
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int available() throws IOException {
        try {
            return super.available();
        } catch (final IOException e) {
            handleIOException(e);
            return 0;
        }
    }

    /**
     * Invokes the delegate's <code>close()</code> method.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        try {
            in.close();
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>mark(int)</code> method.
     *
     * @param readlimit read ahead limit
     */
    @Override
    public synchronized void mark(final int readlimit) {
        in.mark(readlimit);
    }

    /**
     * Invokes the delegate's <code>reset()</code> method.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public synchronized void reset() throws IOException {
        try {
            in.reset();
        } catch (final IOException e) {
            handleIOException(e);
        }
    }

    /**
     * Invokes the delegate's <code>markSupported()</code> method.
     *
     * @return true if mark is supported, otherwise false
     */
    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    /**
     * Invoked by the read methods before the call is proxied. The number of bytes that the caller wanted to read (1 for
     * the {@link #read()} method, buffer length for {@link #read(byte[])}, etc.) is given as an argument.
     * <p>
     * Subclasses can override this method to add common pre-processing functionality without having to override all the
     * read methods. The default implementation does nothing.
     * <p>
     * Note this method is <em>not</em> called from {@link #skip(long)} or {@link #reset()}. You need to explicitly
     * override those methods if you want to add pre-processing steps also to them.
     *
     * @param n number of bytes that the caller asked to be read
     * @throws IOException if the pre-processing fails
     * @since 2.0
     */
    protected void beforeRead(final int n) throws IOException {
        // no-op
    }

    /**
     * Invoked by the read methods after the proxied call has returned successfully. The number of bytes returned to the
     * caller (or -1 if the end of stream was reached) is given as an argument.
     * <p>
     * Subclasses can override this method to add common post-processing functionality without having to override all the
     * read methods. The default implementation does nothing.
     * <p>
     * Note this method is <em>not</em> called from {@link #skip(long)} or {@link #reset()}. You need to explicitly
     * override those methods if you want to add post-processing steps also to them.
     *
     * @param n number of bytes read, or -1 if the end of stream was reached
     * @throws IOException if the post-processing fails
     * @since 2.0
     */
    protected void afterRead(final int n) throws IOException {
        // no-op
    }

    /**
     * Handle any IOExceptions thrown.
     * <p>
     * This method provides a point to implement custom exception handling. The default behaviour is to re-throw the
     * exception.
     *
     * @param e The IOException thrown
     * @throws IOException if an I/O error occurs
     * @since 2.0
     */
    protected void handleIOException(final IOException e) throws IOException {
        throw e;
    }

}
