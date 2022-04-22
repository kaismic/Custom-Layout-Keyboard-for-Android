semicolon_value = "SC027"
keys = [
    "q", "w", "f", "p", "g", "j", "l", "u", "y",
    "a", "s", "d", "t", "r", "h", "e", "k", "i", "o",
    "z", "x", "c", "v", "b", "n", "m"
]

        # <Button
        #     android:id="@+id/key_3"
        #     android:layout_width="@string/common_key_width"
        #     android:layout_height="match_parent"
        #     android:textAllCaps="false"
        #     android:padding="0dp"
        #     android:textSize="@string/common_key_text_size"
        #     android:text="3"

text_file = open("btn_xml_code", "a")

for key in keys:
        text_file.write('        <Button\n')
        text_file.write('            android:id="@+id/key_3"\n')
        text_file.write('            android:layout_width="@string/common_key_width"\n')
        text_file.write('            android:layout_height="match_parent"\n')
        text_file.write('            android:textAllCaps="false"\n')
        text_file.write('            android:padding="0dp"\n')
        text_file.write('            android:textSize="@string/common_key_text_size"\n')
        text_file.write('            android:text="3"\n')
        text_file.write('            />\n')


text_file.close()
print("Script writing finished.")