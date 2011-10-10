package grisu.gricli.command;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.BatchJobException;
import grisu.control.exceptions.NoSuchJobException;
import grisu.gricli.Gricli;
import grisu.gricli.GricliRuntimeException;
import grisu.gricli.completors.JobnameCompletor;
import grisu.gricli.environment.GricliEnvironment;
import grisu.gricli.util.ServiceInterfaceUtils;


public class KillJobCommand implements
GricliCommand {
	private final String jobFilter;
	private final boolean clean;

	@SyntaxDescription(command={"kill","jobs"})
	@AutoComplete(completors={JobnameCompletor.class})
	public KillJobCommand(){
		this("*",true);
	}

	@SyntaxDescription(command={"kill","job"}, arguments={"jobname"})
	@AutoComplete(completors={JobnameCompletor.class})
	public KillJobCommand(String jobFilter){
		this(jobFilter, false);
	}

	public KillJobCommand(String jobFilter, boolean clean) {
		this.jobFilter = jobFilter;
		this.clean = clean;
	}

	public GricliEnvironment execute(GricliEnvironment env)
			throws GricliRuntimeException {
		ServiceInterface si = env.getServiceInterface();
		for (String j : ServiceInterfaceUtils.filterJobNames(si, jobFilter)) {
			boolean refreshJobnameList = false;
			if (this instanceof CleanJobCommand) {
				env.printMessage("cleaning job " + j);
				refreshJobnameList = true;
			} else {
				env.printMessage("killing job " + j);
			}
			try {
				si.kill(j, clean);
			} catch (NoSuchJobException ex) {
				env.printError("job " + j + " does not exist");
			} catch (BatchJobException ex) {
				env.printError("job "+ j + ": "+ ex.getMessage());
			} catch (Exception ex) {
				env.printError("job " + j + ":" + ex.getMessage());
			} finally {
				if (refreshJobnameList) {
					Gricli.completionCache.refreshJobnames();
				}
			}
		}

		return env;
	}

}
