package UniSpace.model.user;

import UniSpace.enums.Faculty;
import UniSpace.enums.ManagerType;
import UniSpace.enums.UserRole;

public class Manager extends Employee {

    private ManagerType managerType;

    public Manager() {}

    public Manager(String id, String firstName, String lastName,
                   String email, String password,
                   Faculty department, double salary, ManagerType managerType) {
        super(id, firstName, lastName, email, password,
                UserRole.MANAGER, department, salary);
        this.managerType = managerType;
    }

    public ManagerType getManagerType() { return managerType; }
    public void setManagerType(ManagerType managerType) { this.managerType = managerType; }

    @Override
    public String toString() {
        return super.toString() + String.format(" | ManagerType: %s", managerType);
    }
}
