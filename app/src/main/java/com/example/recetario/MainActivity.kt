package com.example.recetario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// =================== THEME (simple) ===================
private val Orange = Color(0xFFF97316) // orange-500
private val OrangeDark = Color(0xFFEA580C) // orange-600
private val Green = Color(0xFF16A34A) // green-600
private val CardBorder = Color(0xFFE5E7EB) // gray-200
private val MutedText = Color(0xFF6B7280) // gray-500

@Composable
fun RecetarioThemeM3(content: @Composable () -> Unit) {
    val colorScheme = androidx.compose.material3.lightColorScheme(
        primary = Orange,
        onPrimary = Color.White,
        secondary = Green,
        surface = Color.White,
        background = Color(0xFFFFFBFF)
    )
    MaterialTheme(colorScheme = colorScheme, content = content)
}

// =================== DATA ===================
data class Ingredient(val id: String, val name: String)
data class Recipe(
    val id: String,
    val name: String,
    val ingredients: List<String>,
    val preparation: String
)

enum class ViewRoute { Main, Results, Detail, Create, AddIngredient }

// =================== ACTIVITY ===================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecetarioThemeM3 {
                RecetarioApp()
            }
        }
    }
}

// =================== ROOT APP ===================
@Composable
fun RecetarioApp() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ----- App State (simplificado sin savers personalizados) -----
    var currentView by remember { mutableStateOf(ViewRoute.Main) }

    val allIngredients = remember {
        mutableStateListOf(
            Ingredient("1", "Lechuga"),
            Ingredient("2", "Arroz"),
            Ingredient("3", "Tomate"),
            Ingredient("4", "Huevo"),
            Ingredient("5", "Pollo"),
            Ingredient("6", "Queso"),
            Ingredient("7", "Fideo largo"),
            Ingredient("8", "Ajo"),
        )
    }

    val recipes = remember {
        mutableStateListOf(
            Recipe(
                id = "1",
                name = "Majadito",
                ingredients = listOf("Arroz", "Huevo", "Pollo"),
                preparation = """
                    1. Cocinar el arroz hasta que esté suave.
                    2. Freír el pollo en trozos pequeños.
                    3. Freír los huevos.
                    4. Mezclar todo y servir caliente con plátano frito opcional.
                """.trimIndent()
            ),
            Recipe(
                id = "2",
                name = "Arroz a la valenciana",
                ingredients = listOf("Arroz", "Pollo", "Ajo"),
                preparation = """
                    1. Sofreír el ajo picado.
                    2. Agregar el pollo cortado en cubos.
                    3. Añadir el arroz y agua, cocinar hasta que esté listo.
                    4. Servir caliente con verduras al gusto.
                """.trimIndent()
            ),
            Recipe(
                id = "3",
                name = "Arroz con huevo",
                ingredients = listOf("Arroz", "Huevo"),
                preparation = """
                    1. Cocinar el arroz.
                    2. Freír los huevos al gusto.
                    3. Servir el arroz con el huevo encima.
                    4. Agregar sal y pimienta al gusto.
                """.trimIndent()
            ),
            Recipe(
                id = "4",
                name = "Pollo con arroz y huevo",
                ingredients = listOf("Pollo", "Arroz", "Huevo"),
                preparation = """
                    1. Cocinar el pollo con especias.
                    2. Preparar el arroz por separado.
                    3. Freír los huevos.
                    4. Combinar todo en un plato y servir.
                """.trimIndent()
            ),
        )
    }

    val selectedIngredients = remember { mutableStateListOf<String>() }
    var matchedRecipes by remember { mutableStateOf(listOf<Recipe>()) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

    // Create Recipe form
    var newRecipeName by remember { mutableStateOf("") }
    val newRecipeIngredients = remember { mutableStateListOf<String>() }
    var newRecipePreparation by remember { mutableStateOf("") }

    // Add Ingredient form
    var newIngredientName by remember { mutableStateOf("") }

    fun showToast(title: String, description: String? = null) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = if (description.isNullOrBlank()) title else "$title\n$description",
                withDismissAction = true
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            when (currentView) {
                ViewRoute.Main -> {
                    MainView(
                        allIngredients = allIngredients,
                        selected = selectedIngredients,
                        onToggle = { name ->
                            if (selectedIngredients.contains(name)) {
                                selectedIngredients.remove(name)
                            } else {
                                selectedIngredients.add(name)
                            }
                        },
                        onAddIngredient = { currentView = ViewRoute.AddIngredient },
                        onSearch = {
                            if (selectedIngredients.isEmpty()) {
                                showToast(
                                    "Selecciona ingredientes",
                                    "Debes seleccionar al menos un ingrediente."
                                )
                            } else {
                                matchedRecipes = recipes.filter { r ->
                                    selectedIngredients.all { it in r.ingredients }
                                }
                                currentView = ViewRoute.Results
                            }
                        }
                    )
                }

                ViewRoute.Results -> {
                    ResultsView(
                        matched = matchedRecipes,
                        onBack = { currentView = ViewRoute.Main },
                        onCreateNew = { currentView = ViewRoute.Create },
                        onOpen = { r ->
                            selectedRecipe = r
                            currentView = ViewRoute.Detail
                        }
                    )
                }

                ViewRoute.Detail -> {
                    selectedRecipe?.let { r ->
                        DetailView(
                            recipe = r,
                            onBack = { currentView = ViewRoute.Results }
                        )
                    }
                }

                ViewRoute.Create -> {
                    CreateRecipeView(
                        allIngredients = allIngredients,
                        name = newRecipeName,
                        onNameChange = { newRecipeName = it },
                        selectedIngredients = newRecipeIngredients,
                        onToggleIngredient = { ing ->
                            if (newRecipeIngredients.contains(ing)) newRecipeIngredients.remove(ing)
                            else newRecipeIngredients.add(ing)
                        },
                        preparation = newRecipePreparation,
                        onPreparationChange = { newRecipePreparation = it },
                        onBack = { currentView = ViewRoute.Results },
                        onSave = {
                            when {
                                newRecipeName.isBlank() -> showToast(
                                    "Nombre requerido",
                                    "Debes ponerle un nombre a la receta."
                                )
                                newRecipeIngredients.isEmpty() -> showToast(
                                    "Ingredientes requeridos",
                                    "Debes seleccionar al menos un ingrediente."
                                )
                                newRecipePreparation.isBlank() -> showToast(
                                    "Preparación requerida",
                                    "Debes escribir la preparación."
                                )
                                else -> {
                                    val newR = Recipe(
                                        id = System.currentTimeMillis().toString(),
                                        name = newRecipeName.trim(),
                                        ingredients = newRecipeIngredients.toList(),
                                        preparation = newRecipePreparation.trim()
                                    )
                                    recipes.add(newR)
                                    showToast("¡Receta creada!", "${newR.name} se agregó a tus recetas.")
                                    newRecipeName = ""
                                    newRecipeIngredients.clear()
                                    newRecipePreparation = ""
                                    currentView = ViewRoute.Main
                                }
                            }
                        }
                    )
                }

                ViewRoute.AddIngredient -> {
                    AddIngredientView(
                        name = newIngredientName,
                        onNameChange = { newIngredientName = it },
                        onBack = { currentView = ViewRoute.Main },
                        onAdd = {
                            val trimmed = newIngredientName.trim()
                            when {
                                trimmed.isBlank() -> showToast(
                                    "Nombre requerido",
                                    "Debes escribir el nombre del ingrediente."
                                )
                                allIngredients.any { it.name.equals(trimmed, ignoreCase = true) } -> showToast(
                                    "Ingrediente duplicado",
                                    "Este ingrediente ya existe."
                                )
                                else -> {
                                    allIngredients.add(Ingredient(System.currentTimeMillis().toString(), trimmed))
                                    showToast("¡Ingrediente agregado!", "$trimmed se agregó a tu lista.")
                                    newIngredientName = ""
                                    currentView = ViewRoute.Main
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// =================== REUSABLE UI ===================
@Composable
fun AppTopRow(title: String, onBack: (() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        if (onBack != null) {
            androidx.compose.material3.IconButton(onClick = onBack) {
                androidx.compose.material3.Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
            Spacer(Modifier.width(4.dp))
        }
        // Ícono básico para evitar material-icons-extended
        androidx.compose.material3.Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = Orange,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AppButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    variant: String = "default",
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    when (variant) {
        "outline" -> OutlinedButton(
            onClick = onClick,
            shape = shape,
            border = BorderStroke(2.dp, Orange),
            modifier = modifier
        ) {
            if (icon != null) { icon(); Spacer(Modifier.width(8.dp)) }
            Text(text, color = Orange, fontWeight = FontWeight.Medium)
        }
        "ghost" -> TextButton(onClick = onClick, shape = shape, modifier = modifier) {
            if (icon != null) { icon(); Spacer(Modifier.width(8.dp)) }
            Text(text, color = Color(0xFF374151))
        }
        else -> Button(
            onClick = onClick,
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.White),
            modifier = modifier,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            if (icon != null) { icon(); Spacer(Modifier.width(8.dp)) }
            Text(text, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, CardBorder),
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun AppInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = singleLine,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Orange,
            cursorColor = Orange
        )
    )
}

@Composable
fun AppTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 6,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = false,
        minLines = minLines,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Orange,
            cursorColor = Orange
        )
    )
}

@Composable
fun AppCheckbox(checked: Boolean) {
    val boxColor = if (checked) Orange else CardBorder
    val bg = if (checked) Orange else Color.Transparent
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg,
        border = BorderStroke(2.dp, boxColor),
        modifier = Modifier.size(22.dp)
    ) {
        if (checked) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("✓", color = Color.White, modifier = Modifier.scale(1.1f))
            }
        }
    }
}

// =================== SCREENS ===================

@Composable
fun MainView(
    allIngredients: List<Ingredient>,
    selected: List<String>,
    onToggle: (String) -> Unit,
    onAddIngredient: () -> Unit,
    onSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF7ED), Color.White)
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            AppTopRow(title = "Mis Ingredientes")
            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                content = {
                    items(allIngredients, key = { it.id }) { ing ->
                        val isSelected = ing.name in selected
                        AppCard(
                            modifier = Modifier,
                            onClick = { onToggle(ing.name) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppCheckbox(checked = isSelected)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    ing.name,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF111827)
                                )
                            }
                        }
                    }
                }
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(Color.White.copy(alpha = 0.95f)),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppButton(
                    text = "Agregar ingrediente",
                    variant = "outline",
                    icon = { androidx.compose.material3.Icon(Icons.Filled.Add, contentDescription = null) },
                    onClick = onAddIngredient,
                    modifier = Modifier.weight(1f)
                )
                AppButton(
                    text = "Buscar receta",
                    icon = { androidx.compose.material3.Icon(Icons.Filled.Search, contentDescription = null) },
                    onClick = onSearch,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ResultsView(
    matched: List<Recipe>,
    onBack: () -> Unit,
    onCreateNew: () -> Unit,
    onOpen: (Recipe) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF7ED), Color.White)
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AppTopRow(title = "Resultados", onBack = onBack)
        if (matched.isNotEmpty()) {
            Text("Recetas encontradas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(matched, key = { it.id }) { r ->
                    AppCard(onClick = { onOpen(r) }) {
                        Text(r.name, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Ingredientes: ${r.ingredients.joinToString()}",
                            color = MutedText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            Spacer(Modifier.height(24.dp))
            Column(
                Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Ícono básico para estado vacío
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text("No encontramos recetas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "No existe una receta con esos ingredientes.\n¿Quieres crear una nueva?",
                    color = MutedText
                )
                Spacer(Modifier.height(16.dp))
                AppButton(
                    text = "Crear nueva receta",
                    onClick = onCreateNew,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    icon = { androidx.compose.material3.Icon(Icons.Filled.Add, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
fun DetailView(
    recipe: Recipe,
    onBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF7ED), Color.White)
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AppTopRow(title = "Detalle", onBack = onBack)
        AppCard {
            Text(recipe.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = OrangeDark)
            Spacer(Modifier.height(16.dp))
            Text("Ingredientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Green)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                recipe.ingredients.forEach { ing ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(Orange, RoundedCornerShape(percent = 50)))
                        Spacer(Modifier.width(8.dp))
                        Text(ing)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Preparación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Green)
            Spacer(Modifier.height(8.dp))
            Text(recipe.preparation)
        }
    }
}

@Composable
fun CreateRecipeView(
    allIngredients: List<Ingredient>,
    name: String,
    onNameChange: (String) -> Unit,
    selectedIngredients: List<String>,
    onToggleIngredient: (String) -> Unit,
    preparation: String,
    onPreparationChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF7ED), Color.White)
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AppTopRow(title = "Crear receta", onBack = onBack)
        Spacer(Modifier.height(8.dp))
        Text("Nombre de la receta", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        AppInput(value = name, onValueChange = onNameChange, placeholder = "Ej: Ensalada mixta")

        Spacer(Modifier.height(16.dp))
        Text("Selecciona los ingredientes", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(min = 0.dp).weight(1f, fill = false)
        ) {
            items(allIngredients, key = { it.id }) { ing ->
                val sel = ing.name in selectedIngredients
                AppCard(onClick = { onToggleIngredient(ing.name) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppCheckbox(sel); Spacer(Modifier.width(8.dp))
                        Text(ing.name)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Preparación", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        AppTextArea(
            value = preparation,
            onValueChange = onPreparationChange,
            placeholder = "Escribe los pasos para preparar la receta..."
        )

        Spacer(Modifier.height(12.dp))
        AppButton(
            text = "Guardar receta",
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun AddIngredientView(
    name: String,
    onNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onAdd: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF7ED), Color.White)
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AppTopRow(title = "Agregar ingrediente", onBack = onBack)
        Spacer(Modifier.height(8.dp))
        Text("Nombre del ingrediente", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        AppInput(
            value = name,
            onValueChange = onNameChange,
            placeholder = "Ej: Cebolla",
            singleLine = true,
        )
        Spacer(Modifier.height(16.dp))
        AppButton(
            text = "Agregar",
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth(),
            icon = { androidx.compose.material3.Icon(Icons.Filled.Add, contentDescription = null) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMain() {
    RecetarioThemeM3 { RecetarioApp() }
}
