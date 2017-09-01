package Utils;

public class Triple<A, B, C> {
	
	protected final A first;
	protected final B second;
	protected final C third;
	
	public Triple(A first, B second, C third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}

	public C getThird() {
		return third;
	}

	private static boolean equals(Object x, Object y) {
		return ((x == null) && (y == null)) || ((x != null) && x.equals(y));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object other) {
		return (other instanceof Triple) && equals(first, ((Triple<A, B, C>) other).first)
				&& equals(second, ((Triple<A, B, C>) other).second);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null)  ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        result = prime * result + ((third == null)  ? 0 : third.hashCode());
        return result;
	}

	@Override
	public String toString() {
		return "(" + first + "," + second + "," + third +")";
	}
}
