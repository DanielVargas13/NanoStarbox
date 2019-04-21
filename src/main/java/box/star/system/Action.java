package box.star.system;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class Action extends Process implements Runnable, Cloneable {

    private class Pipe {
        PipedOutputStream output = new PipedOutputStream();
        PipedInputStream input = new PipedInputStream();
        Pipe(){
            try {
                output.connect(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        void close(){
            try {
                input.close();
                output.close();
            } catch (Exception e){}
        }
    }

    private Thread thread;
    private String[] parameters;

    private Pipe p_stdin, p_stdout, p_stderr;

    protected BufferedInputStream stdin;
    protected BufferedOutputStream stdout, stderr;

    protected Environment environment;
    protected int exitValue;

    /**
     * Override
     * @return the command name
     */
    public String toString(){
        return "builtin";
    }

    final void start(Environment environment, String[] parameters){
        this.environment = environment;
        this.parameters = parameters;
        p_stdin = new Pipe();
        p_stdout = new Pipe();
        p_stderr = new Pipe();
        stdin = new BufferedInputStream(p_stdin.input);
        stdout = new BufferedOutputStream(p_stdout.output);
        stderr = new BufferedOutputStream(p_stderr.output);
        thread = new Thread(this);
        thread.start();
    }

    public void onException(Exception e){}

    @Override
    final public void run() {
        try {
            main(parameters);
        } catch (Exception e){onException(e);}
        finally {
            try {
                stdin.close();
                stdout.close();
                stderr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void main(String[] parameters){}

    /**
     * Returns the exit value for the subprocess.
     *
     * @return the exit value of the subprocess represented by this
     * {@code Process} object.  By convention, the value
     * {@code 0} indicates normal termination.
     * @throws IllegalThreadStateException if the subprocess represented
     *                                     by this {@code Process} object has not yet terminated
     */
    @Override
    final public int exitValue() {
        if (thread.isAlive()) throw new IllegalThreadStateException("process not terminated");
        return exitValue;
    }

    /**
     * Tests whether the subprocess represented by this {@code Process} is
     * alive.
     *
     * @return {@code true} if the subprocess represented by this
     * {@code Process} object has not yet terminated.
     * @since 1.8
     */
    @Override
    final public boolean isAlive() {
        return thread.isAlive();
    }

    /**
     * Returns the output stream connected to the normal input of the
     * subprocess.  Output to the stream is piped into the standard
     * input of the process represented by this {@code Process} object.
     *
     * <p>Implementation note: It is a good idea for the returned
     * output stream to be buffered.
     *
     * @return the output stream connected to the normal input of the
     * subprocess
     */
    @Override
    final public OutputStream getOutputStream() {
        return new BufferedOutputStream(p_stdin.output);
    }

    /**
     * Returns the input stream connected to the normal output of the
     * subprocess.  The stream obtains data piped from the standard
     * output of the process represented by this {@code Process} object.
     *
     * <p>Implementation note: It is a good idea for the returned
     * input stream to be buffered.
     *
     * @return the input stream connected to the normal output of the
     * subprocess
     */
    @Override
    final public InputStream getInputStream() {
        return new BufferedInputStream(p_stdout.input);
    }

    /**
     * Returns the input stream connected to the error output of the
     * subprocess.  The stream obtains data piped from the error output
     * of the process represented by this {@code Process} object.
     *
     * <p>Implementation note: It is a good idea for the returned
     * input stream to be buffered.
     *
     * @return the input stream connected to the error output of
     * the subprocess
     */
    @Override
    final public InputStream getErrorStream() {
        return new BufferedInputStream(p_stderr.input);
    }

    /**
     * Causes the current thread to wait, if necessary, until the
     * process represented by this {@code Process} object has
     * terminated.  This method returns immediately if the subprocess
     * has already terminated.  If the subprocess has not yet
     * terminated, the calling thread will be blocked until the
     * subprocess exits.
     *
     * @return the exit value of the subprocess represented by this
     * {@code Process} object.  By convention, the value
     * {@code 0} indicates normal termination.
     * @throws InterruptedException if the current thread is
     *                              {@linkplain Thread#interrupt() interrupted} by another
     *                              thread while it is waiting, then the wait is ended and
     *                              an {@link InterruptedException} is thrown.
     */
    @Override
    final public int waitFor() throws InterruptedException {
        thread.join();
        return exitValue;
    }

    /**
     * Causes the current thread to wait, if necessary, until the
     * subprocess represented by this {@code Process} object has
     * terminated, or the specified waiting time elapses.
     *
     * <p>If the subprocess has already terminated then this method returns
     * immediately with the value {@code true}.  If the process has not
     * terminated and the timeout value is less than, or equal to, zero, then
     * this method returns immediately with the value {@code false}.
     *
     * <p>The default implementation of this methods polls the {@code exitValue}
     * to check if the process has terminated. Concrete implementations of this
     * class are strongly encouraged to override this method with a more
     * efficient implementation.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the {@code timeout} argument
     * @return {@code true} if the subprocess has exited and {@code false} if
     * the waiting time elapsed before the subprocess has exited.
     * @throws InterruptedException if the current thread is interrupted
     *                              while waiting.
     * @throws NullPointerException if unit is null
     * @since 1.8
     */
    @Override
    final public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
        if (! thread.isAlive()) return true;
        if (timeout <= 0) return false;

        long remainingNanos  = unit.toNanos(timeout);
        long deadline = System.nanoTime() + remainingNanos ;

        do {
            // Round up to next millisecond
            long msTimeout = TimeUnit.NANOSECONDS.toMillis(remainingNanos + 999_999L);
            thread.wait(msTimeout);
            //waitForTimeoutInterruptibly(handle, msTimeout);
            if (Thread.interrupted())
                throw new InterruptedException();
            if (! thread.isAlive()) {
                return true;
            }
            remainingNanos = deadline - System.nanoTime();
        } while (remainingNanos > 0);
        return (! thread.isAlive());
    }

    /**
     * Kills the subprocess. Whether the subprocess represented by this
     * {@code Process} object is forcibly terminated or not is
     * implementation dependent.
     */
    @Override
    final public void destroy() {
        try {
            stdin.close();
            stdout.close();
            stderr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Kills the subprocess. The subprocess represented by this
     * {@code Process} object is forcibly terminated.
     *
     * <p>The default implementation of this method invokes {@link #destroy}
     * and so may not forcibly terminate the process. Concrete implementations
     * of this class are strongly encouraged to override this method with a
     * compliant implementation.  Invoking this method on {@code Process}
     * objects returned by {@link ProcessBuilder#start} and
     * {@link Runtime#exec} will forcibly terminate the process.
     *
     * <p>Note: The subprocess may not terminate immediately.
     * i.e. {@code isAlive()} may return true for a brief period
     * after {@code destroyForcibly()} is called. This method
     * may be chained to {@code waitFor()} if needed.
     *
     * @return the {@code Process} object representing the
     * subprocess to be forcibly destroyed.
     * @since 1.8
     */
    @Override
    final public Process destroyForcibly() {
        destroy();
        return this;
    }

    /**
     * Creates and returns a copy of this object.  The precise meaning
     * of "copy" may depend on the class of the object. The general
     * intent is that, for any object {@code x}, the expression:
     * <blockquote>
     * <pre>
     * x.clone() != x</pre></blockquote>
     * will be true, and that the expression:
     * <blockquote>
     * <pre>
     * x.clone().getClass() == x.getClass()</pre></blockquote>
     * will be {@code true}, but these are not absolute requirements.
     * While it is typically the case that:
     * <blockquote>
     * <pre>
     * x.clone().equals(x)</pre></blockquote>
     * will be {@code true}, this is not an absolute requirement.
     * <p>
     * By convention, the returned object should be obtained by calling
     * {@code super.clone}.  If a class and all of its superclasses (except
     * {@code Object}) obey this convention, it will be the case that
     * {@code x.clone().getClass() == x.getClass()}.
     * <p>
     * By convention, the object returned by this method should be independent
     * of this object (which is being cloned).  To achieve this independence,
     * it may be necessary to modify one or more fields of the object returned
     * by {@code super.clone} before returning it.  Typically, this means
     * copying any mutable objects that comprise the internal "deep structure"
     * of the object being cloned and replacing the references to these
     * objects with references to the copies.  If a class contains only
     * primitive fields or references to immutable objects, then it is usually
     * the case that no fields in the object returned by {@code super.clone}
     * need to be modified.
     * <p>
     * The method {@code clone} for class {@code Object} performs a
     * specific cloning operation. First, if the class of this object does
     * not implement the interface {@code Cloneable}, then a
     * {@code CloneNotSupportedException} is thrown. Note that all arrays
     * are considered to implement the interface {@code Cloneable} and that
     * the return type of the {@code clone} method of an array type {@code T[]}
     * is {@code T[]} where T is any reference or primitive type.
     * Otherwise, this method creates a new instance of the class of this
     * object and initializes all its fields with exactly the contents of
     * the corresponding fields of this object, as if by assignment; the
     * contents of the fields are not themselves cloned. Thus, this method
     * performs a "shallow copy" of this object, not a "deep copy" operation.
     * <p>
     * The class {@code Object} does not itself implement the interface
     * {@code Cloneable}, so calling the {@code clone} method on an object
     * whose class is {@code Object} will result in throwing an
     * exception at run time.
     *
     * @return a clone of this instance.
     * @throws CloneNotSupportedException if the object's class does not
     *                                    support the {@code Cloneable} interface. Subclasses
     *                                    that override the {@code clone} method can also
     *                                    throw this exception to indicate that an instance cannot
     *                                    be cloned.
     * @see Cloneable
     */
    final protected Object createBuiltin() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Override
     * tries to use this builtin to resolve a complex query of any form.
     * @param command the command name specified
     * @return true if this builtin will handle this execution request.
     */
    public boolean match(String command){
        return false;
    }

}
