package ua.procamp.locking;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Program {
    public Long id;
    public String name;
    public Long version;
}
