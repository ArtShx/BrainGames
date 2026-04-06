

## Architecture Hierarchy
To keep your project organized, follow this structure:

### GameEngine (The Calculator) 
Pure logic. "If the grid is 3x3, what are the coordinates?" "Create a board object based on these inputs."
The Controller is the Manager of a single game session. It holds the "Mutable" data (the current sequence, the hearts left, the current index).
Responsibility: Managing the lifecycle and flow of a specific game.
What it does:
Holds the mutableListOf<BoardCoordinate>().
Tracks hearts and inputIndex.
Decides when the game is "Over" or "Solved".
Crucially: It translates a raw "Tap" into a "Game Result."
Why: It keeps the ViewModel clean. The ViewModel doesn't need to know how Simon Says works; it just asks the Controller to process a tap.

### GameController (The Referee)
The Controller is the Manager of a single game session. It holds the "Mutable" data (the current sequence, the hearts left, the current index).
Responsibility: Managing the lifecycle and flow of a specific game.
What it does:
- Holds the mutableListOf<BoardCoordinate>().
- Tracks hearts and inputIndex.
- Decides when the game is "Over" or "Solved".
- Crucially: It translates a raw "Tap" into a "Game Result."
Why: It keeps the ViewModel clean. The ViewModel doesn't need to know how Simon Says works; it just asks the Controller to process a tap.

### 3. The Game ViewModel (The "UI State Holder")
The ViewModel is the Bridge between the Android OS and your Game Logic.
Responsibility: Exposing data to Compose and handling UI-related timing (like the 420ms delay).
What it does:
- Holds the StateFlow<GameSnapshot>.
- Handles viewModelScope for animations/delays.
- Calls the Controller when the user interacts with the screen.
Why: It handles the "Android" side of things (Lifecycles, Coroutines) so your Controller and Engine can stay as "Plain Old Kotlin Objects."


### How they should interact (The "Flow")
1. User Taps Cell (Row 1, Col 2)
2. ViewModel receives the tap. It calls viewModelScope.launch. 
3. ViewModel asks Controller: "What happens if I tap 1, 2?
4. Controller checks its internal sequence. It asks Engine to generate a new BoardState with a highlight. 
5. Controller returns a "Highlighted Snapshot" to the ViewModel. 
6. ViewModel updates the _snapshot.value (UI lights up). 
7. ViewModel calls delay(420). 
8. ViewModel asks Controller: "Okay, animation done. Give me the final state now."
9. Controller returns the final state (Next round or Game Over).