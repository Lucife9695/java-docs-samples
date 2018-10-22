/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dlp.snippets;

// [START dlp_inspect_file]
import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.ByteContentItem;
import com.google.privacy.dlp.v2.ByteContentItem.BytesType;
import com.google.privacy.dlp.v2.ContentItem;
import com.google.privacy.dlp.v2.Finding;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.InspectContentRequest;
import com.google.privacy.dlp.v2.InspectContentResponse;
import com.google.privacy.dlp.v2.ProjectName;
import com.google.protobuf.ByteString;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class InspectFile {

  // Inspects the specified file.
  public static void inspectFile(String projectId, String filePath, String fileType) {
    // String projectId = "my-project-id";
    // String filePath = "path/to/image.png";
    // String fileType = "IMAGE"

    // Initialize client with try-with-resources for automatic cleanup of background resources
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      // Set project for request
      ProjectName project = ProjectName.of(projectId);

      // Set content for request
      ByteString fileBytes = ByteString.readFrom(new FileInputStream(filePath));
      ByteContentItem byteItem = ByteContentItem.newBuilder()
          .setType(BytesType.valueOf(fileType))
          .setData(fileBytes)
          .build();
      ContentItem item = ContentItem.newBuilder()
          .setByteItem(byteItem)
          .build();

      // Set required InfoTypes for inspection config
      List<InfoType> infoTypes = new ArrayList<>();
      // See https://cloud.google.com/dlp/docs/infotypes-reference for complete list of info types
      for (String typeName : new String[] {"PHONE_NUMBER", "EMAIL_ADDRESS", "CREDIT_CARD_NUMBER"}) {
        infoTypes.add(InfoType.newBuilder().setName(typeName).build());
      }

      // Set the inspect configuration for request
      InspectConfig config = InspectConfig.newBuilder()
          .addAllInfoTypes(infoTypes)
          .setIncludeQuote(true)
          .build();

      // Construct the request to be sent by the client
      InspectContentRequest request = InspectContentRequest.newBuilder()
          .setParent(project.toString())
          .setItem(item)
          .setInspectConfig(config)
          .build();

      // Use the client to send the request and parse results
      InspectContentResponse response = dlp.inspectContent(request);
      System.out.println("Findings: " + response.getResult().getFindingsCount());
      for (Finding f : response.getResult().getFindingsList()) {
        System.out.println("\tQuote: " + f.getQuote());
        System.out.println("\tInfo type: " + f.getInfoType());
        System.out.println("\tLikelihood: " + f.getLikelihood());
      }
    } catch (Exception e) {
      System.out.println("Error during inspectFile: \n" + e.toString());
    }
  }
}
// [END dlp_inspect_file]
