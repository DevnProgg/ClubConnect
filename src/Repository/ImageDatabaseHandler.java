package Repository;
 import java.awt.*;
 import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import javax.imageio.ImageIO;
import javax.swing.*;
/**
 * Handles database operations related to storing and retrieving images (BLOBs)
 * for users (profile pictures) and clubs (logos).
 * It manages file validation (size limit) and conversion between file and {@code BufferedImage}/{@code ImageIcon}.
 */
public class ImageDatabaseHandler {

    private final Connection connection;

    /**
     * Constructs an {@code ImageDatabaseHandler} with a database connection.
     *
     * @param conn The active database connection.
     */
    public ImageDatabaseHandler(Connection conn) {
        this.connection = conn;
    }

    /**
     * Saves a user's profile picture to the database, updating the corresponding columns.
     * The image file is validated against a 5MB size limit before upload.
     *
     * @param userId The unique ID of the user.
     * @param imageFile The image file to upload.
     * @return {@code true} if the image was successfully saved, {@code false} otherwise.
     */
    public boolean saveUserProfilePicture(long userId, File imageFile) {
        String sql = "UPDATE system_users SET Profile_Picture = ?, Profile_Picture_Type = ?, " +
                "Profile_Picture_Size = ? WHERE User_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             FileInputStream fis = new FileInputStream(imageFile)) {

            String fileName = imageFile.getName();
            String mimeType = getMimeType(fileName);
            long fileSize = imageFile.length();

            /**
             * Validate image size to ensure it does not exceed the 5MB limit.
             */
            if (fileSize > 5 * 1024 * 1024) {
                JOptionPane.showMessageDialog(null,
                        "Image size exceeds 5MB limit!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Set parameters
            pstmt.setBinaryStream(1, fis, (int) fileSize);
            pstmt.setString(2, mimeType);
            pstmt.setInt(3, (int) fileSize);
            pstmt.setLong(4, userId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null,
                        "Profile picture uploaded successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                return true;
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error uploading image: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return false;
    }

    /**
     * Saves a club's logo to the database, updating the corresponding columns in the {@code clubs} table.
     * The logo file is validated against a 5MB size limit before upload.
     *
     * @param clubId The unique ID of the club.
     * @param imageFile The logo file to upload.
     * @return {@code true} if the logo was successfully saved, {@code false} otherwise.
     */
    public boolean saveClubLogo(long clubId, File imageFile) {
        String sql = "UPDATE clubs SET Logo = ?, Logo_Type = ?, Logo_Size = ? WHERE Club_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             FileInputStream fis = new FileInputStream(imageFile)) {

            String fileName = imageFile.getName();
            String mimeType = getMimeType(fileName);
            long fileSize = imageFile.length();

            // Validate image size
            if (fileSize > 5 * 1024 * 1024) {
                JOptionPane.showMessageDialog(null,
                        "Logo size exceeds 5MB limit!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            pstmt.setBinaryStream(1, fis, (int) fileSize);
            pstmt.setString(2, mimeType);
            pstmt.setInt(3, (int) fileSize);
            pstmt.setLong(4, clubId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null,
                        "Club logo uploaded successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                return true;
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error uploading logo: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return false;
    }

    /**
     * Retrieves a user's profile picture as a {@code BufferedImage} from the database.
     *
     * @param userId The unique ID of the user.
     * @return The retrieved {@code BufferedImage} or {@code null} if no image is found or an error occurs.
     */
    public BufferedImage getUserProfilePicture(long userId) {
        String sql = "SELECT Profile_Picture FROM system_users WHERE User_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                byte[] imageBytes = rs.getBytes("Profile_Picture");
                /**
                 * Converts the byte array (BLOB data) from the database into a BufferedImage.
                 */
                if (imageBytes != null) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                    return ImageIO.read(bis);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves a club's logo as a {@code BufferedImage} from the database.
     *
     * @param clubId The unique ID of the club.
     * @return The retrieved {@code BufferedImage} or {@code null} if no image is found or an error occurs.
     */
    public BufferedImage getClubLogo(long clubId) {
        String sql = "SELECT logo FROM clubs WHERE Club_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, clubId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                byte[] imageBytes = rs.getBytes("logo");
                /**
                 * Converts the byte array (BLOB data) from the database into a BufferedImage.
                 */
                if (imageBytes != null) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                    return ImageIO.read(bis);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves a user's profile picture, scales it, and returns it as an {@code ImageIcon}
     * suitable for display in Swing components like {@code JLabel}.
     *
     * @param userId The unique ID of the user.
     * @param width The desired width for the icon.
     * @param height The desired height for the icon.
     * @return An {@code ImageIcon} of the scaled profile picture, or {@code null} if no image is found.
     */
    public ImageIcon getUserProfilePictureIcon(long userId, int width, int height) {
        BufferedImage image = getUserProfilePicture(userId);
        if (image != null) {
            Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        }
        return null;
    }

    /**
     * Retrieves a club's logo, scales it, and returns it as an {@code ImageIcon}
     * suitable for display in Swing components like {@code JLabel}.
     *
     * @param clubId The unique ID of the club.
     * @param width The desired width for the icon.
     * @param height The desired height for the icon.
     * @return An {@code ImageIcon} of the scaled club logo, or {@code null} if no image is found.
     */
    public ImageIcon getClubLogoIcon(long clubId, int width, int height) {
        BufferedImage image = getClubLogo(clubId);
        if (image != null) {
            Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        }
        return null;
    }

    /**
     * Deletes a user's profile picture by setting the image and metadata columns to {@code NULL}
     * in the {@code system_users} table.
     *
     * @param userId The unique ID of the user.
     * @return {@code true} if the picture was successfully deleted (updated), {@code false} otherwise.
     */
    public boolean deleteUserProfilePicture(int userId) {
        String sql = "UPDATE system_users SET Profile_Picture = NULL, " +
                "Profile_Picture_Type = NULL, Profile_Picture_Size = NULL WHERE User_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a club's logo by setting the image and metadata columns to {@code NULL}
     * in the {@code clubs} table.
     *
     * @param clubId The unique ID of the club.
     * @return {@code true} if the logo was successfully deleted (updated), {@code false} otherwise.
     */
    public boolean deleteClubLogo(int clubId) {
        String sql = "UPDATE clubs SET Logo = NULL, Logo_Type = NULL, Logo_Size = NULL WHERE Club_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, clubId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Determines the MIME type (content type) of a file based on its extension.
     * This is crucial for correctly storing image metadata in the database.
     *
     * @param fileName The name of the file, including its extension.
     * @return The corresponding MIME type string (e.g., "image/jpeg") or "application/octet-stream" if the extension is unknown.
     */
    private String getMimeType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * Checks the database to determine if a specific user has an existing profile picture.
     *
     * @param userId The unique ID of the user.
     * @return {@code true} if the {@code Profile_Picture} column is not {@code NULL}, {@code false} otherwise.
     */
    public boolean userHasProfilePicture(int userId) {
        String sql = "SELECT Profile_Picture FROM system_users WHERE User_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBytes("Profile_Picture") != null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Retrieves the metadata (MIME type and size) of a user's profile picture from the database.
     *
     * @param userId The unique ID of the user.
     * @return An {@code ImageMetadata} object containing the picture's type and size, or {@code null} if no metadata is found.
     */
    public ImageMetadata getUserImageMetadata(int userId) {
        String sql = "SELECT Profile_Picture_Type, Profile_Picture_Size FROM system_users WHERE User_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new ImageMetadata(
                        rs.getString("Profile_Picture_Type"),
                        rs.getInt("Profile_Picture_Size")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * A simple data class to hold the metadata (MIME type and size) of an image stored in the database.
     */
    public static class ImageMetadata {
        public final String mimeType;
        public final int size;

        /**
         * Constructs an {@code ImageMetadata} object.
         *
         * @param mimeType The MIME type (e.g., "image/jpeg").
         * @param size The size of the image in bytes.
         */
        public ImageMetadata(String mimeType, int size) {
            this.mimeType = mimeType;
            this.size = size;
        }

        /**
         * Converts the image size from bytes to a formatted string in kilobytes (KB).
         *
         * @return The size formatted as "X.XX KB".
         */
        public String getSizeInKB() {
            return String.format("%.2f KB", size / 1024.0);
        }
    }
}