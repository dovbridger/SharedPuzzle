package puzzle;

public enum Orientation {
	UP, DOWN, LEFT, RIGHT;

	public Orientation opposite(){
		switch(this.ordinal()){
		case 0:
			return DOWN;
		case 1:
			return UP;
		case 2:
			return RIGHT;
		case 3:
			return LEFT;
		}
		return null;
	}
	
	public static void main(String[] args){
		Orientation or = Orientation.DOWN;
		System.out.println(or);
	}
}

