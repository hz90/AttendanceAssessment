package com.test.pro;

import org.openjdk.jol.info.ClassLayout;

public class Test {
	public static class T {
		String str = "sss";
	}

	public static void main(String[] args) {
		T t = new T();
		String str = ClassLayout.parseInstance(t).toPrintable();
		System.out.println(str);
		switch (str) {
		case "a":
			System.out.println("a");
			break;
		}
	}

}
