package com.bgu.android.tetris;

public class ITetrino extends Tetrino {
	private static final int BLOCK_TYPE = TileView.BLOCK_BLUE;
	public static final int I_SIZE = 4;	
	public ITetrino(int x, int y) {
		super(x, y);
		sMap = new int[I_SIZE][I_SIZE];
		for(int col = 0; col < I_SIZE; col++) {
			for(int row = 0; row < I_SIZE; row++) {
				sMap[col][row] = 0;
			}
		}
		this.sMap[0][0] = 0;
		this.sMap[0][1] = 0;
		this.sMap[0][2] = 0;
		this.sMap[0][3] = 0;
		this.sMap[1][0] = BLOCK_TYPE;
		this.sMap[1][1] = BLOCK_TYPE;
		this.sMap[1][2] = BLOCK_TYPE;
		this.sMap[1][3] = BLOCK_TYPE;
		this.sMap[2][0] = 0;
		this.sMap[2][1] = 0;
		this.sMap[2][2] = 0;
		this.sMap[2][3] = 0;
		this.sMap[3][0] = 0;
		this.sMap[3][1] = 0;
		this.sMap[3][2] = 0;
		this.sMap[3][3] = 0;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean rotateTetrino(TetrinoMap map) {
		int[][] temp = new int[I_SIZE][I_SIZE];
		for(int col = 0; col < I_SIZE; col++){
			for(int row = 0; row < I_SIZE; row++) {
				temp[col][row] = sMap[row][3-col];
			}
		}
		if(!isColusionX(getXPos(), temp, map) && !isColusionY(getYPos(), temp, map)) {
			sMap = temp;
			return true;
		}
		return false;
	}
	
	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return I_SIZE;
	}
}