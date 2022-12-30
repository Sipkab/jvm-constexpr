package sipka.jvm.constexpr.main;

import sipka.cmdline.api.Command;
import sipka.cmdline.api.SubCommand;

@Command(className = "sipka.jvm.constexpr.main.CliMain",
		helpCommand = { "-h", "help", "-help", "--help", "?", "/?" },
		main = true)
@SubCommand(name = "licenses", type = LicensesCommand.class)
@SubCommand(name = "run", type = RunCommand.class, defaultCommand = true)
public class MainCommand {
}
