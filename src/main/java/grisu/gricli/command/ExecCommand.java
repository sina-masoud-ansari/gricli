package grisu.gricli.command;

import grisu.gricli.GricliRuntimeException;
import grisu.gricli.command.exec.AsyncProcessStreamReader;
import grisu.gricli.environment.GricliEnvironment;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecCommand implements GricliCommand {

	private static Logger myLogger = LoggerFactory.getLogger(ExecCommand.class);

	public static void main(String[] args) throws Exception {

		final ExecCommand c = new ExecCommand("ls -lah", "");
		c.execute(new GricliEnvironment());
	}

	private final String cmd;
	private final LinkedList<String> args;
	private final boolean isAsync;

	@SyntaxDescription(command = { "exec" }, arguments = { "command",
	"arguments" })
	public ExecCommand(String... cmd) {
		this.cmd = cmd[0];
		this.args = new LinkedList<String>(Arrays.asList(cmd));
		this.args.remove(0);

		if ((this.args.size() > 0)
				&& "&".equals(this.args.get(this.args.size() - 1))) {
			this.isAsync = true;
			this.args.remove(this.args.size() - 1);
		} else {
			this.isAsync = false;
		}
	}

	public GricliEnvironment execute(GricliEnvironment env)
			throws GricliRuntimeException {

		// downloading files that start with grid:// or gsiftp://
		// not sure whether we should do that, can always disable later on
		// for (int i=0; i < args.size(); i++ ) {
		// String arg = args.get(i);
		// if ( arg.startsWith(ServiceInterface.VIRTUAL_GRID_PROTOCOL_NAME) ||
		// arg.startsWith("gsiftp") ) {
		// myLogger.debug("Downloading file: " + arg);
		// FileManager fm = env.getGrisuRegistry().getFileManager();
		// File tmp = null;
		// try {
		// tmp = fm.downloadFile(arg);
		// } catch (FileTransactionException e) {
		// throw new GricliRuntimeException(
		// "Can't access remote file: " + arg, e);
		// }
		// args.set(i, tmp.getPath());
		// myLogger.debug("Downloaded file and exchanging remote url for exec command: "
		// + tmp.getPath());
		// }
		// }

		final List<String> cmdList = new LinkedList<String>(args);
		cmdList.add(0, cmd);

		final ProcessBuilder builder = new ProcessBuilder(cmdList);
		final Map<String, String> environ = builder.environment();
		builder.directory(new File(System.getProperty("user.dir")));

		Process process;
		try {
			process = builder.start();
		} catch (final IOException e) {
			myLogger.error(e.getLocalizedMessage(), e);
			env.printError(e.getLocalizedMessage());

			return env;
		}

		final AsyncProcessStreamReader reader = new AsyncProcessStreamReader(
				new GricliEnvironment(), process);

		if (!isAsync) {
			try {
				final int exitValue = reader.waitForProcessToFinish();
				// maybe put exit value in env?
				// env.printMessage("Process finished with value: " +
				// exitValue);
			} catch (final InterruptedException e) {
				env.printError(e.getLocalizedMessage());
			}
		}

		return env;
	}

}
