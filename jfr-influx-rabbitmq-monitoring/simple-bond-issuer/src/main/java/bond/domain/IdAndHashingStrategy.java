package bond.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
@ToString
public abstract class IdAndHashingStrategy {

    @Column(nullable = false, insertable = false, updatable = false)
    @CreatedDate
    @ColumnDefault("now()")
    private Instant createdDate;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        IdAndHashingStrategy that = (IdAndHashingStrategy) obj;
        return id != null && id.equals(that.id);
    }

}
