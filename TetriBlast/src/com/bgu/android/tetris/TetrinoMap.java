package com.bgu.android.tetris;

public class TetrinoMap {
	public static final int MAP_X_SIZE = 10;
	public static final int MAP_Y_SIZE = 20;
	private int[][] map;
	//private HashMap<Point, Integer> map;
	
	public TetrinoMap() {
		map = new int[MAP_X_SIZE][MAP_Y_SIZE];
		this.resetMap();
		
	}
	
	public TetrinoMap(int x, int y) {
		//TODO implement this for any map size
	}
	
	public void resetMap() {
		for(int x = 0; x < MAP_X_SIZE; x++) {
			for(int y = 0; y < MAP_Y_SIZE;y++) {
				map[x][y] = 0;
			}
		}
	}
	
	public void putTetrinoOnMap(Tetrino shape) {
		for(int col = 0; col < shape.getSize(); col++){
			for(int row = 0; row < shape.getSize(); row++) {
				//TODO need to check map bounds
				map[shape.getXPos()+col][shape.getYPos()+row] = shape.sMap[col][row];
			}
		}
	}
	
	public int getMapValue(int x, int y) {
		return map[x][y];
	}
}
