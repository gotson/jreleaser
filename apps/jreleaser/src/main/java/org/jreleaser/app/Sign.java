/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.app;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.signer.Signer;
import org.jreleaser.signer.SigningException;
import org.jreleaser.tools.Checksums;
import picocli.CommandLine;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "sign",
    description = "Sign release artifacts")
public class Sign extends AbstractModelCommand {
    @Override
    protected void consumeModel(JReleaserModel jreleaserModel) {
        try {
            JReleaserContext context = createContext(jreleaserModel);

            Checksums.collectAndWriteChecksums(context);
            Signer.sign(context);
        } catch (SigningException e) {
            throw new JReleaserException("Unexpected error when signing release " + actualConfigFile.toAbsolutePath(), e);
        }
    }
}