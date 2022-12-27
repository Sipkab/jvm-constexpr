package sipka.jvm.constexpr.main;

import sipka.cmdline.api.Command;
import sipka.cmdline.api.SubCommand;

@Command(className = "sipka.jvm.constexpr.main.CliMain",
		helpCommand = { "-h", "help", "-help", "--help", "?", "/?" },
		main = true)
@SubCommand(name = "licenses", type = LicensesCommand.class)
public class MainCommand {

	public void call() {
		//TODO implement main command 
		throw new UnsupportedOperationException();
	}
}
