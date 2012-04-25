package com.bgu.android.tetris;

public class JTetrino extends Tetrino {
	private static final int BLOCK_TYPE = TileView.BLOCK_LIGHBLUE;
	public JTetrino(int x, int y) {
		super(x, y);
		this.sMap[0][0] = 0;
		this.sMap[0][1] = 0;
		this.sMap[0][2] = 0;
		this.sMap[1][0] = 0;
		this.sMap[1][1] = 0;
		this.sMap[1][2] = BLOCK_TYPE;
		this.sMap[2][0] = BLOCK_TYPE;
		this.sMap[2][1] = BLOCK_TYPE;
		this.sMap[2][2] = BLOCK_TYPE;
		// TODO Auto-generated constructor stub
	}

}