import com.tgirard12.graphqlkotlindsl.models.SimpleTypes
import com.tgirard12.graphqlkotlindsl.models.User
import java.util.*

object Stubs {
    val users = listOf(
            User(id = UUID.fromString("b6214ea0-fc5a-493c-91ea-939e17b2e95f"), name = "John", email = "john@mail.com", deleteField = 1),
            User(id = UUID.fromString("c682a4c5-e66b-4dbf-a077-d97579c308dc"), name = "Doe", email = "doe@mail.com", deleteField = 2)
    )

    val simpleTypes = listOf(
            SimpleTypes(1, 2, 3, 4, 5.1f, 5.2f, 6.1, 6.2,
                    "val", "null val",
                    UUID.fromString("dac5310f-484b-4f81-9756-bce0349ceaa5"), UUID.fromString("acb53d26-3cba-4177-ba54-88232b5066c5"),
                    null),
            SimpleTypes(10, null, 30, null, 5.1f, null, 6.1, null,
                    "val 10", null,
                    UUID.fromString("b10fc3b8-b96b-4344-9430-ff7c861dc1f6"), null,
                    null),
            SimpleTypes(100, 200, 300, 400, 500.1f, 500.2f, 600.1, 600.2,
                    "val 100", "null val 100",
                    UUID.fromString("49307d83-a4cc-4452-8fdc-6462843e8f66"), UUID.fromString("454473e5-2673-4e45-a429-50bdd449c0fb"),
                    null)
    )
}