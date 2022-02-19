package capers;

// import jdk.internal.icu.impl.Norm2AllModes;
import jdk.jshell.execution.Util;

import java.io.*;

import static capers.Utils.*;

/** Represents a dog that can be serialized.
 * @author Moksha
*/
public class Dog implements Serializable {

    /** Folder that dogs live in. */
    static final File DOG_FOLDER = Utils.join(".capers", "dogs"); // (hint: look at the `join'
                                         //      function in Utils)

    /** Age of dog. */
    private int age;
    /** Breed of dog. */
    private String breed;
    /** Name of dog. */
    private String name;

    /**
     * Creates a dog object with the specified parameters.
     * @param name Name of dog
     * @param breed Breed of dog
     * @param age Age of dog
     */
    public Dog(String name, String breed, int age) {
        this.age = age;
        this.breed = breed;
        this.name = name;
    }

    /**
     * Reads in and deserializes a dog from a file with name NAME in DOG_FOLDER.
     *
     * @param name Name of dog to load
     * @return Dog read from file
     */
    public static Dog fromFile(String name) {
        // (hint: look at the Utils file)
        Dog d = readObject(join(DOG_FOLDER, name), Dog.class);
        return d;
    }

    /**
     * Increases a dog's age and celebrates!
     */
    public void haveBirthday() {
        age += 1;
        System.out.println(toString());
        System.out.println("Happy birthday! Woof! Woof!");
    }

    /**
     * Saves a dog to a file for future use.
     */
    public void saveDog() {
        // (hint: don't forget dog names are unique)
        File out = join(DOG_FOLDER, this.name);
        if (out.exists()) {
            return;
        }
        writeObject(out, new Dog(this.name, this.breed, this.age));
    }

    @Override
    public String toString() {
        return String.format(
            "Woof! My name is %s and I am a %s! I am %d years old! Woof!",
            name, breed, age);
    }

}
