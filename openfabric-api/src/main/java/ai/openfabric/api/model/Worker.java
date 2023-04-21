package ai.openfabric.api.model;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.List;

@Entity()
public class Worker extends Datable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "of-uuid")
    @GenericGenerator(name = "of-uuid", strategy = "ai.openfabric.api.model.IDGenerator")
    @Getter
    @Setter
    public String id;

    public Worker() {
    }

    public Worker(String name, String containerId, String command, String image) {
        this.name = name;
        this.containerId = containerId;
        this.command = command;
        this.image = image;
    }

    @Getter
    public String name;

    @Getter
    public String containerId;

    @Getter
    public String command;

    @Getter
    public String image;

    @Getter
    @Setter
    public String hostPort;

    @Getter
    @Setter
    public String containerPort;

    @Getter
    @Setter
    public String status;

}
