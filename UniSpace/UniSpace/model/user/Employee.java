package UniSpace.model.user;

import UniSpace.enums.UserRole;
import java.io.Serializable;

public abstract class Employee extends User implements Serializable {

    private String department;
    private double salary;

    public Employee() {}

    public Employee(String id, String firstName, String lastName,
                    String email, String password, UserRole role,
                    String department, double salary) {
        super(id, firstName, lastName, email, password, role);
        this.department = department;
        this.salary = salary;
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Dept: %s", department);
    }
}
