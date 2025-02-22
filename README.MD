# XeXeGUI
A new way to make GUIs.

# Setup

First of all, you need to add the library as a dependency.
```kotlin
repositories {
    maven("https://jitpack.io")
}

val xexeGuiCommit = "e86d0bcc49" // aka version
                                // Look it up at: https://jitpack.io/#NazarXeXe/XeXeGui
dependencies {
    implementation("com.github.NazarXeXe.XeXeGui:core:${xexeGuiCommit}")
    implementation("com.github.NazarXeXe.XeXeGui:coroutine:${xexeGuiCommit}")
    implementation("com.github.NazarXeXe.XeXeGui:adventure:${xexeGuiCommit}")
    implementation("com.github.NazarXeXe.XeXeGui:route:${xexeGuiCommit}")
    implementation("com.github.NazarXeXe.XeXeGui:pagination:${xexeGuiCommit}")
}

// If you use shadowJar:
tasks.shadowJar {
    relocate("me.nazarxexe.ui", "my.package.libs.ui")
}
```
Then, let's make a gui handler:
```kt
import me.nazarxexe.ui.GuiHandler

object MyGuiHandler: GuiHandler()
```
To open the gui to player you need to:
```kt
import me.nazarxexe.ui.gui

MyGuiHandler.openTo(
    player,
    gui { /* ... */ }
)
```
# GUIs
## Simple gui
```kotlin
val myGui = gui {
    click { it.isCancelled = true } // Prevents player from picking up items.
    component(0 /* slot */) {
        var count by state(0) // This is delegated variable.
                            // When variable is changed, component re-renders.
        button { // Listens to InventoryClickEvent
            count++ // This is where the update happens.
        }
        render {
            val item = ItemStack(Material.REDSTONE)
            val mat = item.itemMeta!!
            mat.setDisplayName("$count")
            item.itemMeta = mat
            item // Shows the item
        }
    }
}

// Show
MyGuiHandler.openTo(player, myGui)
```
Result:
<br />
<img src="https://github.com/NazarXeXe/XeXeGui/blob/main/readme/ezgui.gif?raw=true">
## Pagination
```kotlin
val myGui = gui {
    click {
        it.isCancelled = true
    }
    // Pagination reserves 7 slots.
    // You can use pagination() but I need to generate new page before users goes to another page.
    // Cache if you want to keep the components when the user switches to another page.
    val pagination = dynPagination(10..16, cache = true) { thePage: Int ->
        val page = Page(this)
        for (i in 0..6) {
            // Keep in mind that slots should be around range of page!
            page.set(i + 10, component {
                render {
                    ItemStack(Material.DIRT).also { itm ->
                        itm.itemMeta!!.also {
                            it.setDisplayName("${i + (6 * thePage)}")
                            itm.itemMeta = it
                        }
                    }
                }
            })
        }
        page
    }
    
    pagination.page = 0 // Renders components at first page.
    
    component(21) { // Previous
        // hook(pagination.pageSignal)
        // The hook will re-render the component when page is changed.
        // It would be useful if you want to hide/show the component when the user is on the n page.
        button {
            if (pagination.currentPage > 0)
                pagination.page--
        }
        render {
            ItemStack(Material.ARROW)
                .also { itm ->
                    itm.itemMeta!!.also {
                        it.setDisplayName("Prev")
                        itm.itemMeta = it
                    }
                }
        }
    }
    component(23) { // Next
        // hook(pagination.pageSignal)
        button {
            pagination.page++
        }
        render {
            ItemStack(Material.ARROW)
                .also { itm ->
                    itm.itemMeta!!.also {
                        it.setDisplayName("Next")
                        itm.itemMeta = it
                    }
                }
        }
    }
}

// Show
MyGuiHandler.openTo(player, myGui)
```
Result:
<br/>
<img src="https://github.com/NazarXeXe/XeXeGui/blob/main/readme/pagination.gif?raw=true">
## Async
I'm gonna use ktor for web requests and gson for json serialization:
```kotlin
dependencies {
    implementation("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-cio:3.0.0")
    implementation("com.google.code.gson:gson:2.11.0")
}
```
```kotlin
data class ProgrammerQuote(val author: String, val quote: String)

val scheduler = object : Scheduler {
    override fun run(runnable: () -> Unit): BukkitTask {
        return plugin.server.scheduler.runTask(plugin, runnable)
    }

    override fun runRepeat(repeat: Int, runnable: () -> Unit): BukkitTask {
        return plugin.server.scheduler.runTaskTimer(plugin, runnable, 0, repeat.toLong())
    }
}

val myGui = gui {
    // HTTP client (ktor)
    val client = HttpClient(CIO)
    click { it.isCancelled = true }
    suspense(0, scheduler) {
        val shimmerSignal = shimmer(scheduler)
        fallback {
            // Cool shimmer state that re-render the component every tick.
            // It will stop when gui is closed or suspending component is done.
            val shimmer by shimmerSignal
            button {
                it.whoClicked.sendMessage("I'm still loading.")
            }
            render {
                ItemStack(Material.PLAYER_HEAD).also { itm ->
                    itm.componentItemMeta.also {
                        it.displayName = shimmer.applyTo("||||||||||||||||||")
                        it.lore(shimmer.applyTo(
                            "|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||"
                        ))
                        itm.componentItemMeta = it
                    }
                }
            }
        }
        suspendingComponent {
            delay(2000)
            val theQuote = Gson().fromJson(
                // REQUEST
                client.get("https://programming-quotesapi.vercel.app/api/random")
                .bodyAsText(), ProgrammerQuote::class.java)
            shimmerSignal.task.cancel() // Stop shimmer after finish.
            button {
                it.whoClicked.sendMessage("I'm loaded :)")
            }
            render {
                ItemStack(Material.PLAYER_HEAD).also { itm ->
                    itm.itemMeta!!.also {
                        it.setDisplayName(theQuote.author)
                        it.lore = listOf(theQuote.quote)
                        itm.itemMeta = it
                    }
                }
            }
        }
    }
}

// Show
MyGuiHandler.openTo(player, myGui)
```
Result:
<br />
<img src="https://github.com/NazarXeXe/XeXeGui/blob/main/readme/async.gif?raw=true">

# Configurable GUI
Many people perfer configurable gui, but it's very complex to make guis configurable. This library attemts to solve this problem.
<br/>
Let's make simple configurable gui:
```kotlin
val myGui = configuredGui("gui") { // It's blueprint you shouldn't create multiple instances.
    val canTake by configuredField<Boolean>("canTake") // It will be null in this scope
    gui { // This block of code executed when blueprint starts making process
        click { it.isCancelled = !(canTake ?: false) } // canTake should be not null 
                                                    // Because it's configured in this block.
    }
    configuredComponent("myComponent") {
        val header by configuredField<String>("header") // It will be null in this scope
        component { // Every configured field should be configured in this block.
            render {
                ItemStack(Material.DIAMOND).also {
                    it.componentItemMeta.also { m ->
                        m.displayName = minimessage(header ?: "Unknown")
                        it.componentItemMeta = m
                    }
                }
            }
        }
    }
}

myGui.visit(ConifgSection(this@Plugin.config, "gui")) // Now it's configured.
                                                    // Call it again when config is reloaded
MyGuiHandler.openTo(player, myGui.make())
```
In config:
```yml
gui:
  canTake: false
  inventory_size: 27 # Chest
  blueprint:
    # 9 columns 3 rows
    - "aaaaaaaaa" # 9
    - ""
    - ""
  myComponent:
    character: a
    header: "<red>Hi"
```
Result:
<img src="https://github.com/NazarXeXe/XeXeGui/blob/main/readme/2024-12-10_17.44.24.png?raw=true">
