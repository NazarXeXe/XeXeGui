import be.seeseemelk.mockbukkit.MockBukkit
import me.nazarxexe.ui.*
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import kotlin.test.Test
import kotlin.test.assertEquals

class TestGui {

    @Test
    fun testBasic() {

        val server = MockBukkit.mock()
        val plugin = MockBukkit.createMockPlugin()
        val gh = GuiHandler()

        plugin.server.pluginManager.registerEvents(
            gh, plugin
        )

        val theGui = gui(InventoryType.CHEST.defaultSize) {
            val rizz = state(1000)
            click {
                it.isCancelled = true
            }
            component(0) {
                var count by state(0)
                var theRizz by hook(rizz)

                button {
                    theRizz--
                }
                button {
                    count++
                }
                render {
                    val itm = ItemStack(Material.STONE)
                    val mat = itm.itemMeta ?: error("Air.")
                    mat.setDisplayName("$count")
                    itm.setItemMeta(mat)
                    itm
                }
            }
            component(1) {
                val theRizz by hook(rizz)
                render {
                    val itm = ItemStack(Material.STONE)
                    val mat = itm.itemMeta ?: error("Air.")
                    mat.setDisplayName("$theRizz")
                    itm.itemMeta = mat
                    itm
                }
            }

        }

        val player = server.addPlayer(":3")

        gh.openTo(player, theGui)
        assertEquals(theGui.raw()[0].itemMeta?.displayName, "0")
        assertEquals(theGui.raw()[1].itemMeta?.displayName, "1000")

        player.simulateInventoryClick(0)
        assertEquals(theGui.raw()[0].itemMeta?.displayName, "1")
        assertEquals(theGui.raw()[1].itemMeta?.displayName, "999")

        player.closeInventory()

        assertEquals(gh.list.size, 0)

        MockBukkit.unmock()
    }


    @Test
    fun testShimmer() {

    }
}