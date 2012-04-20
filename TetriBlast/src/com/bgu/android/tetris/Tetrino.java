package com.bgu.android.tetris;

import android.graphics.Point;

public class Tetrino {
	public static final int SIZE = 3;
	public int[][] sMap;
	private Point pos;
	
	public Tetrino(int x, int y) {
		sMap = new int[SIZE][SIZE];
		for(int col = 0; col < SIZE; col++) {
			for(int row = 0; row < SIZE; row++) {
				sMap[col][row] = 0;
			}
		}
		fillMap();
		pos = new Point(x,y);
	}
	
	//TODO remove this
	private void fillMap(){
		sMap[0][0] = TileView.BLOCK_PINK;
		sMap[0][1] = TileView.BLOCK_PINK;
		sMap[0][2] = 0;
		sMap[1][0] = 0;
		sMap[1][1] = TileView.BLOCK_PINK;
		sMap[1][2] = TileView.BLOCK_PINK;
		sMap[2][0] = 0;
		sMap[2][1] = 0;
		sMap[2][2] = 0;
	}
	
	//TODO need to check
	public void rotateTetrino() {
		int[][] temp = new int[SIZE][SIZE];
		for(int col = 0; col < SIZE; col++){
			for(int row = 0; row < SIZE; row++) {
				temp[col][row] = sMap[row][2-col];
			}
		}
		sMap = temp;
	}
	
	/**
	 * @return the pos
	 */
	public Point getPos() {
		return pos;
	}

	/**
	 * @param pos the pos to set
	 */
	public void setPos(int x, int y) {
		this.pos.x = x;
		this.pos.y = y;
	}
	
	/**
	 * @return the x position
	 */
	public int getXPos() {
		return pos.x;
	}
	
	/**
	 * @return the y position
	 */
	public int getYPos() {
		return pos.y;
	}

	public int getSize() {
		// TODO Auto-generated method stub
		return SIZE;
	}
	
}
