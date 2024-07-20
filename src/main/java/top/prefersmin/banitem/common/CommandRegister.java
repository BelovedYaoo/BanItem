package top.prefersmin.banitem.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import top.prefersmin.banitem.config.BanItemConfig;

import static com.mojang.text2speech.Narrator.LOGGER;

/**
 * 命令注册类
 *
 * @author PrefersMin
 * @version 1.1
 */
public class CommandRegister {

    /**
     * 注册命令
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("banitem")
                        // 设置命令权限
                        .requires(source -> source.hasPermission(4))
                        // 配置文件重载
                        .then(Commands.literal("configReload")
                                .executes(CommandRegister::configReload)
                        )
        );
    }

    /**
     * 配置文件重载方法
     */
    private static int configReload(CommandContext<CommandSourceStack> context) {
        try {
            LOGGER.info("BanItem：正在重载配置文件，发起者：{}", context.getSource().getPlayerOrException());
        } catch (CommandSyntaxException e) {
            LOGGER.info("BanItem：正在重载配置文件，无发起者");
        }
        new BanItemConfig().reloadConfig();
        return 1;
    }

}
