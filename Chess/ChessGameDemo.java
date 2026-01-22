import java.util.List;
import java.util.ArrayList;

// Enums
enum Color {
    WHITE, BLACK
}

enum GameStatus {
    ACTIVE, CHECK, CHECKMATE, STALEMATE, RESIGNATION, DRAW
}

// Position Class
class Position {
    private int row;
    private int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return row == other.row && col == other.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }
}

// Abstract Piece Class
abstract class Piece {
    protected Color color;
    protected Position position;
    protected boolean isAlive;
    protected boolean hasMoved;

    public Piece(Color color, Position position) {
        this.color = color;
        this.position = position;
        this.isAlive = true;
        this.hasMoved = false;
    }

    public Color getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        this.isAlive = alive;
    }

    public void setPosition(Position position) {
        this.position = position;
        this.hasMoved = true;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public abstract boolean canMove(Board board, Position to);
    public abstract String getSymbol();

    protected boolean isPathClear(Board board, Position from, Position to) {
        int rowDir = Integer.compare(to.getRow() - from.getRow(), 0);
        int colDir = Integer.compare(to.getCol() - from.getCol(), 0);

        int currentRow = from.getRow() + rowDir;
        int currentCol = from.getCol() + colDir;

        while (currentRow != to.getRow() || currentCol != to.getCol()) {
            if (board.getPiece(new Position(currentRow, currentCol)) != null) {
                return false;
            }
            currentRow += rowDir;
            currentCol += colDir;
        }
        return true;
    }
}

// King Class
class King extends Piece {
    public King(Color color, Position position) {
        super(color, position);
    }

    @Override
    public boolean canMove(Board board, Position to) {
        int rowDiff = Math.abs(to.getRow() - position.getRow());
        int colDiff = Math.abs(to.getCol() - position.getCol());

        // King moves one square in any direction
        if (rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff > 0)) {
            Piece targetPiece = board.getPiece(to);
            return targetPiece == null || targetPiece.getColor() != this.color;
        }

        // Castling logic can be added here
        return false;
    }

    @Override
    public String getSymbol() {
        return color == Color.WHITE ? "♔" : "♚";
    }
}

// Queen Class
class Queen extends Piece {
    public Queen(Color color, Position position) {
        super(color, position);
    }

    @Override
    public boolean canMove(Board board, Position to) {
        int rowDiff = Math.abs(to.getRow() - position.getRow());
        int colDiff = Math.abs(to.getCol() - position.getCol());

        // Queen moves like rook or bishop
        boolean straightLine = (rowDiff == 0 || colDiff == 0);
        boolean diagonal = (rowDiff == colDiff);

        if (straightLine || diagonal) {
            Piece targetPiece = board.getPiece(to);
            return isPathClear(board, position, to) &&
                    (targetPiece == null || targetPiece.getColor() != this.color);
        }
        return false;
    }

    @Override
    public String getSymbol() {
        return color == Color.WHITE ? "♕" : "♛";
    }
}

// Rook Class
class Rook extends Piece {
    public Rook(Color color, Position position) {
        super(color, position);
    }

    @Override
    public boolean canMove(Board board, Position to) {
        // Same row OR same column
        boolean sameRow = (to.getRow() == position.getRow());
        boolean sameCol = (to.getCol() == position.getCol());

        if (sameRow ^ sameCol) { // XOR - exactly one must be true
            Piece targetPiece = board.getPiece(to);
            return isPathClear(board, position, to) &&
                    (targetPiece == null || targetPiece.getColor() != this.color);
        }
        return false;
    }

    @Override
    public String getSymbol() {
        return color == Color.WHITE ? "♖" : "♜";
    }
}

// Bishop Class
class Bishop extends Piece {
    public Bishop(Color color, Position position) {
        super(color, position);
    }

    @Override
    public boolean canMove(Board board, Position to) {
        int rowDiff = Math.abs(to.getRow() - position.getRow());
        int colDiff = Math.abs(to.getCol() - position.getCol());

        // Diagonal movement
        if (rowDiff == colDiff && rowDiff > 0) {
            Piece targetPiece = board.getPiece(to);
            return isPathClear(board, position, to) &&
                    (targetPiece == null || targetPiece.getColor() != this.color);
        }
        return false;
    }

    @Override
    public String getSymbol() {
        return color == Color.WHITE ? "♗" : "♝";
    }
}

// Knight Class
class Knight extends Piece {
    public Knight(Color color, Position position) {
        super(color, position);
    }

    @Override
    public boolean canMove(Board board, Position to) {
        int rowDiff = Math.abs(to.getRow() - position.getRow());
        int colDiff = Math.abs(to.getCol() - position.getCol());

        // L-shaped movement: 2+1 or 1+2
        if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) {
            Piece targetPiece = board.getPiece(to);
            return targetPiece == null || targetPiece.getColor() != this.color;
        }
        return false;
    }

    @Override
    public String getSymbol() {
        return color == Color.WHITE ? "♘" : "♞";
    }
}

// Pawn Class
class Pawn extends Piece {
    public Pawn(Color color, Position position) {
        super(color, position);
    }

    @Override
    public boolean canMove(Board board, Position to) {
        int direction = (color == Color.WHITE) ? 1 : -1;
        int rowDiff = to.getRow() - position.getRow();
        int colDiff = Math.abs(to.getCol() - position.getCol());

        // Forward one square
        if (rowDiff == direction && colDiff == 0) {
            return board.getPiece(to) == null;
        }

        // Forward two squares (first move)
        if (!hasMoved && rowDiff == 2 * direction && colDiff == 0) {
            Position intermediate = new Position(position.getRow() + direction, position.getCol());
            return board.getPiece(intermediate) == null && board.getPiece(to) == null;
        }

        // Capture diagonally
        if (rowDiff == direction && colDiff == 1) {
            Piece targetPiece = board.getPiece(to);
            return targetPiece != null && targetPiece.getColor() != this.color;
        }

        // En passant logic can be added here
        return false;
    }

    @Override
    public String getSymbol() {
        return color == Color.WHITE ? "♙" : "♟";
    }
}

// Board Class
class Board {
    private Piece[][] cells;

    public Board() {
        cells = new Piece[8][8];
        initializeBoard();
    }

    private void initializeBoard() {
        // Place white pieces
        cells[0][0] = new Rook(Color.WHITE, new Position(0, 0));
        cells[0][1] = new Knight(Color.WHITE, new Position(0, 1));
        cells[0][2] = new Bishop(Color.WHITE, new Position(0, 2));
        cells[0][3] = new Queen(Color.WHITE, new Position(0, 3));
        cells[0][4] = new King(Color.WHITE, new Position(0, 4));
        cells[0][5] = new Bishop(Color.WHITE, new Position(0, 5));
        cells[0][6] = new Knight(Color.WHITE, new Position(0, 6));
        cells[0][7] = new Rook(Color.WHITE, new Position(0, 7));

        for (int i = 0; i < 8; i++) {
            cells[1][i] = new Pawn(Color.WHITE, new Position(1, i));
        }

        // Place black pieces
        cells[7][0] = new Rook(Color.BLACK, new Position(7, 0));
        cells[7][1] = new Knight(Color.BLACK, new Position(7, 1));
        cells[7][2] = new Bishop(Color.BLACK, new Position(7, 2));
        cells[7][3] = new Queen(Color.BLACK, new Position(7, 3));
        cells[7][4] = new King(Color.BLACK, new Position(7, 4));
        cells[7][5] = new Bishop(Color.BLACK, new Position(7, 5));
        cells[7][6] = new Knight(Color.BLACK, new Position(7, 6));
        cells[7][7] = new Rook(Color.BLACK, new Position(7, 7));

        for (int i = 0; i < 8; i++) {
            cells[6][i] = new Pawn(Color.BLACK, new Position(6, i));
        }
    }

    public Piece getPiece(Position position) {
        if (!position.isValid()) return null;
        return cells[position.getRow()][position.getCol()];
    }

    public void setPiece(Position position, Piece piece) {
        if (position.isValid()) {
            cells[position.getRow()][position.getCol()] = piece;
        }
    }

    public void displayBoard() {
        System.out.println("  a b c d e f g h");
        for (int i = 7; i >= 0; i--) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < 8; j++) {
                Piece piece = cells[i][j];
                System.out.print(piece == null ? ". " : piece.getSymbol() + " ");
            }
            System.out.println((i + 1));
        }
        System.out.println("  a b c d e f g h");
    }
}

// Move Class
class Move {
    private Piece piece;
    private Position from;
    private Position to;
    private Piece capturedPiece;

    public Move(Piece piece, Position from, Position to) {
        this.piece = piece;
        this.from = from;
        this.to = to;
    }

    public Piece getPiece() {
        return piece;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public void setCapturedPiece(Piece piece) {
        this.capturedPiece = piece;
    }

    public boolean isValid(Board board, Player currentPlayer) {
        // 1. Check if piece belongs to current player
        if (piece.getColor() != currentPlayer.getColor()) {
            return false;
        }

        // 2. Check if destination is valid
        if (!to.isValid()) {
            return false;
        }

        // 3. Call piece.canMove(board, to)
        if (!piece.canMove(board, to)) {
            return false;
        }

        // 4. Check if move puts own king in check (simplified - actual implementation would be more complex)
        // This would require a method to simulate the move and check for threats to the king

        return true;
    }
}

// Player Class
class Player {
    private Color color;
    private List<Piece> pieces;
    private String name;

    public Player(Color color, String name) {
        this.color = color;
        this.name = name;
        this.pieces = new ArrayList<>();
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public void addPiece(Piece piece) {
        pieces.add(piece);
    }

    public boolean makeMove(Move move, Board board) {
        if (!move.isValid(board, this)) {
            return false;
        }

        Position from = move.getFrom();
        Position to = move.getTo();
        Piece piece = move.getPiece();

        // Capture piece if present
        Piece capturedPiece = board.getPiece(to);
        if (capturedPiece != null) {
            capturedPiece.setAlive(false);
            move.setCapturedPiece(capturedPiece);
        }

        // Update board
        board.setPiece(from, null);
        board.setPiece(to, piece);
        piece.setPosition(to);

        return true;
    }
}

// Game Class
class Game {
    private Board board;
    private Player[] players;
    private Player currentPlayer;
    private List<Move> moveHistory;
    private GameStatus status;

    public Game(String player1Name, String player2Name) {
        this.board = new Board();
        this.players = new Player[2];
        this.players[0] = new Player(Color.WHITE, player1Name);
        this.players[1] = new Player(Color.BLACK, player2Name);
        this.currentPlayer = players[0]; // White starts
        this.moveHistory = new ArrayList<>();
        this.status = GameStatus.ACTIVE;

        initializePlayers();
    }

    private void initializePlayers() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board.getPiece(new Position(i, j));
                if (piece != null) {
                    if (piece.getColor() == Color.WHITE) {
                        players[0].addPiece(piece);
                    } else {
                        players[1].addPiece(piece);
                    }
                }
            }
        }
    }

    public boolean makeMove(Move move) {
        if (status != GameStatus.ACTIVE) {
            System.out.println("Game is not active!");
            return false;
        }

        if (currentPlayer.makeMove(move, board)) {
            moveHistory.add(move);

            // Switch player
            currentPlayer = (currentPlayer == players[0]) ? players[1] : players[0];

            // Check game status
            updateGameStatus();

            return true;
        }
        return false;
    }

    private void updateGameStatus() {
        // Simplified - actual implementation would check for check, checkmate, stalemate
        // This would involve checking if the king is under attack and if there are valid moves
    }

    public boolean isCheckmate() {
        // Implementation to check if current player's king is in checkmate
        return false;
    }

    public boolean isStalemate() {
        // Implementation to check if current player has no valid moves but is not in check
        return false;
    }

    public void displayBoard() {
        board.displayBoard();
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }
}

// Main Class for Testing
public class ChessGameDemo {
    public static void main(String[] args) {
        Game game = new Game("Alice", "Bob");

        System.out.println("Initial Board:");
        game.displayBoard();

        // Example move: Pawn from e2 to e4
        Piece pawn = game.getCurrentPlayer().getPieces().stream()
                .filter(p -> p instanceof Pawn && p.getPosition().equals(new Position(1, 4)))
                .findFirst().orElse(null);

        if (pawn != null) {
            Move move = new Move(pawn, new Position(1, 4), new Position(3, 4));
            if (game.makeMove(move)) {
                System.out.println("\nAfter move e2 to e4:");
                game.displayBoard();
            }
        }
    }
}