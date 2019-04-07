package ua.procamp.locking;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Program {
    Long id;
    String name;
    Long version;
}
