package br.com.poison.core.arcade.room.map;

import br.com.poison.core.arcade.room.map.synthetic.SyntheticLocation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SignedLocation {

    private final String title;
    private final SyntheticLocation location;

    @Override
    public String toString() {
        return "SignedLocation{" +
                "name='" + title + '\'' +
                ", location=" + location +
                '}';
    }
}