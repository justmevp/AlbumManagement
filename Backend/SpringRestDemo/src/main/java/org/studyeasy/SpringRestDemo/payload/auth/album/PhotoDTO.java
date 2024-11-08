package org.studyeasy.SpringRestDemo.payload.auth.album;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDTO {

    private long id;

    private String name;

    private String description;

    private String fileName;
    
    private String download_link;
}
