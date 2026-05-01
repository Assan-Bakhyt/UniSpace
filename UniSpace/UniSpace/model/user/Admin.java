package UniSpace.model.user;

import UniSpace.enums.Faculty;
import UniSpace.enums.UserRole;

public class Admin extends Employee {

    public Admin() {}

    public Admin(String id, String firstName, String lastName,
                 String email, String password, Faculty department) {
        super(id, firstName, lastName, email, password,
                UserRole.ADMIN, department, 0.0);
    }

    @Override
    public String toString() {
        return super.toString() + " [ADMIN]";
    }
}
