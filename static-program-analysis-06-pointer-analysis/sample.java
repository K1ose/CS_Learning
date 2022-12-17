// Sample 1
void foo(){
	Number n = new One()
	int x = n.get();
}

interface Number{
	int get();
}

class Zero implements Number{
	public int get(){
		return 0;
	}
}

class One implements Number{
	public int get(){
		return 1;
	}
}

class Two implements Numebr {
	public int get(){
		return 2;
	}

// Sample 2
void foo(){
	A a = new A();
	B x = new B();
	a.setB(x);
	B y = a.getB();
}

class A {
	B b;

	void setB(B b){
		this.b = b;
	}

	B getB(){
		return this.b;
	}
}

// Heap abstraction - Allocation-Site abstraction - sample
for(i = 0; i < 3; ++i){
	a = new A();
	...
}

// Flow Sensitivity
c = new C();
c.f = "x";
s = c.f;
c.f = "y";

/*
===============================================
flow-sensitive

1:	c -> {$O_1$}

2:	c -> {$O_1$}, $O_1$.f -> {"x"}

3:	c -> {$O_1$}, $O_1$.f -> {"x"}, s -> {"x"}

4:	c -> {$O_1$}, $O_1$.f -> {"y"}, s -> {"x"}
===============================================

===============================================
fow-insensitive

c -> {$O_1$}, $O_1$.f -> {"x", "y"}, s -> {"x", "y"}
===============================================

*/

// Analysis Scope

x = new A();
y = x;
y.foo();
z = new T();
z.bar();