# chesslin
(WIP) Chess engine written in Kotlin using an object-oriented approach and the square centric 8x8 board representation

## overview
The goal of this project is building my own chess engine, without relying on too much outside information. 
I do not want to simply copy other ideas from other engines, I want to build my own program.
While I'm familiar with a few ideas of chess programming, I want to see how my engine will compare against others.

### implemented
- Board Representation

### to do
- Search
- Evaluation

## Usage

As of right now, this program allows you to play a 2 player chess game (which means not against the computer) inside the terminal.
The only valid inputs are moves which are conforming to the PGN Notation (like "e4" "Ra6" "Qxc7" etc),
 nd "draw" if you want to claim a draw per the threefold repetition rule or the fifty move rule