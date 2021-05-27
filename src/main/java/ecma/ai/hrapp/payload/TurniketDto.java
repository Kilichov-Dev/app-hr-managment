package ecma.ai.hrapp.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TurniketDto {

    @NotNull
    private String ownerEmail;

    @NotNull
    private Integer companyId;

    private boolean enabled = true;
}
