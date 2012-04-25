package com.bgu.android.tetris;

import android.graphics.Point;

public abstract class Tetrino {
	public static final int SIZE = 3;
	public int[][] sMap;
	//public int[][] shadowMap;
	private Point pos;
	
	public Tetrino(int x, int y) {
		sMap = new int[SIZE][SIZE];
		for(int col = 0; col < SIZE; col++) {
			for(int row = 0; row < SIZE; row++) {
				sMap[col][row] = 0;
				//shadowMap[0][0] = 0;
			}
		}
		pos = new Point(x,y);
	}
	
	
	//TODO need to check
	public boolean rotateTetrino(TetrinoMap map) {
		int[][] temp = new int[SIZE][SIZE];
		for(int col = 0; col < SIZE; col++){
			for(int row = 0; row < SIZE; row++) {
				temp[col][row] = sMap[row][2-col];
			}
		}
		if(!isColusionX(this.pos.x, map) && !isColusionY(this.pos.y, map)) {
			sMap = temp;
			return true;
		}
		return false;
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
	public boolean setPos(int x, int y, TetrinoMap map) {
		if(x >= 0 && x < 10) {
			for(int col = 0; col < this.getSize(); col++){
				for(int row = 0; row < this.getSize(); row++) {
					if (sMap[col][row] != TileView.BLOCK_EMPTY) {
						if (x + col >= TetrinoMap.MAP_X_SIZE || x + col < 0 ||
								y + row >= TetrinoMap.MAP_Y_SIZE ||
								map.getMapValue(x + col, y + row) != TileView.BLOCK_EMPTY)
							return false;
					}
				}
			}
		}
		this.pos.x = x;
		this.pos.y = y;
		return true;
	}
	
	protected boolean isColusionY(int newY, TetrinoMap map) {
		// TODO Auto-generated method stub
		if(newY < 20) {
			for(int col = 0; col < this.getSize(); col++){
				for(int row = 0; row < this.getSize(); row++) {
					if (sMap[col][row] != TileView.BLOCK_EMPTY) {
						if (newY + row >= TetrinoMap.MAP_Y_SIZE ||
								map.getMapValue(this.pos.x + col, newY + row) != TileView.BLOCK_EMPTY)
							return true;
					}
				}
			}
		}
		else
			return true;
		//if no collisions 
		return false;
	}
	/**
	 * This function move tetrino down by 1
	 * @param map - to check if possible
	 * @return true is success else false
	 */
	public boolean moveDown(TetrinoMap map) {
		if(!isColusionY(this.pos.y+1,map)) {
			this.pos.y++;
			return true;
		}
		return false;
	}
	
	protected boolean isColusionX(int newX, TetrinoMap map) {
		// TODO Auto-generated method stub
		if(newX >= 0 && newX < 10) {
			for(int col = 0; col < this.getSize(); col++){
				for(int row = 0; row < this.getSize(); row++) {
					if (sMap[col][row] != TileView.BLOCK_EMPTY) {
						if (newX + col >= TetrinoMap.MAP_X_SIZE || newX + col < 0 ||
								map.getMapValue(newX + col, this.pos.y + row) != TileView.BLOCK_EMPTY)
							return true;
					}
				}
			}
		}
		else
			return true;
		//if no collisions 
		return false;
	}


	public boolean moveLeft(TetrinoMap map) {
		if(!isColusionX(this.pos.x-1, map)) {
			this.pos.x--;
			return true;
		}
		return false;
	}
	
	public boolean moveRight(TetrinoMap map) {
		if(!isColusionX(this.pos.x+1, map)) {
			this.pos.x++;
			return true;
		}
		return false;
	}
	
	public boolean drop(TetrinoMap map) {
		//int setY = this.pos.y;
		for (int y = 1; y < 20; y++) {//TODO change to defined values
			if(isColusionY(y, map)) {
				this.pos.y = y-1;
				return true;
			}
		}
		return false;
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
