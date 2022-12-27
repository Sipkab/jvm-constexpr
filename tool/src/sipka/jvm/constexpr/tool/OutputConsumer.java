package sipka.jvm.constexpr.tool;

import java.io.IOException;

import sipka.jvm.constexpr.tool.options.ToolInput;

/**
 * Output consumer interface for the constant inliner.
 * <p>
 * The {@link #put(ToolInput, byte[])} method is called for a processed input class file. Note that if a class file
 * wasn't modified by the inliner tool, this interface may not get called for that input.
 * <p>
 * Clients should implement this interface and handle the results based on their needs.
 */
public interface OutputConsumer {
	/**
	 * Puts the processed output bytes of a given input.
	 * 
	 * @param input
	 *            The original input.
	 * @param resultBytes
	 *            The result bytes after they've been processed by the inliner tool.
	 * @throws IOException
	 *             In case of IO error.
	 */
	public void put(ToolInput<?> input, byte[] resultBytes) throws IOException;
}
